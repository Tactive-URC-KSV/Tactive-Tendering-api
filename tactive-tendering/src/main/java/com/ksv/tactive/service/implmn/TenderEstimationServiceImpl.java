package com.ksv.tactive.service.implmn;

import com.ksv.tactive.dto.*;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.exception.DuplicateEntryException;
import com.ksv.tactive.project.dao.*;
import com.ksv.tactive.project.entity.*;
import com.ksv.tactive.project.entity.Currency;
import com.ksv.tactive.service.MasterService;
import com.ksv.tactive.service.TenderEstimationService;
import com.ksv.tactive.util.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TenderEstimationServiceImpl implements TenderEstimationService {

    @Autowired
    private ProjectDao projectDao;
    @Autowired
    private BOQDao boqDao;
    @Autowired
    private TenderEstimationDao tenderEstDao;
    @Autowired
    private InternalBoqDao internalBoqDao;
    @Autowired
    private AttributeGroupDao attributeGroupDao;
    @Autowired
    private AttributeDao attributeDao;
    @Autowired
    private GlobalValueDao globalValueDao;
    @Autowired
    private MasterService masterService;
    @Autowired
    private MessageSource messageSource;

    private UUID convertToUUID(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid UUID format: " + value);
        }
    }

    @Transactional
    @Override
    public String addResourcesToTender(TenderResourceDto dto) {
        if (dto.getProjectId() == null) throw new RuntimeException("ProjectId missing");
        if (dto.getBoqId() == null) throw new RuntimeException("BOQ Id missing");
        Projects project = projectDao.findById(convertToUUID(dto.getProjectId()))
                .orElseThrow(() -> new RuntimeException("Project not found"));
        TenderEstimation tenderEst = new TenderEstimation();
        TenderEstimation parent = null;
        BOQ boq = null;
        if (dto.isInternal()) {
            String targetId = (dto.getInternalBoqId() != null && !dto.getInternalBoqId().isEmpty()) 
                    ? dto.getInternalBoqId() : dto.getBoqId();
            InternalBoq internalBoq = internalBoqDao.findById(convertToUUID(targetId))
                    .orElseThrow(() -> new RuntimeException("Internal BOQ not found"));
            
            // Set the InternalBoq that this resource BELONGS TO
            tenderEst.setParentInternalBoq(internalBoq);

            project = internalBoq.getProject();
            tenderEst.setProject(project);
            if (internalBoq.getParentResource() != null && !internalBoq.getParentResource().isEmpty()) {
                boq = internalBoq.getParentResource().get(0).getBoq();
                if (boq != null) {
                    tenderEst.setBoq(boq);
                }
            }
        } else {
            boq = boqDao.findById(convertToUUID(dto.getBoqId()))
                    .orElseThrow(() -> new RuntimeException("BOQ not found"));
            tenderEst.setProject(project);
            tenderEst.setBoq(boq);
        }
        UUID resourceId = convertToUUID(dto.getResourceId());
        UUID uomId = convertToUUID(dto.getUomId());
        List<UUID> attributeIds = dto.getAttributes() != null
                ? dto.getAttributes().stream().map(a -> convertToUUID(a.getAttributeId())).toList()
                : new ArrayList<>();
        Optional<TenderEstimation> duplicateOpt =
                findDuplicateResource(
                        parent,
                        boq,
                        project,
                        resourceId,
                        uomId,
                        attributeIds,
                        dto.getResourceNatureId()
                );
        if (duplicateOpt.isPresent()) {
            tenderEst = duplicateOpt.get();
            if (ResourceNature.SIMPLE.name().equalsIgnoreCase(dto.getResourceNatureId())) {
                throw new DuplicateEntryException("This simple resource already exists for this BOQ and Project.");
            }
            // For complex, merge quantity
            double newQty = tenderEst.getNetQuantity() + dto.getNetQuantity();
            tenderEst.setNetQuantity(newQty);
            double totalCost = newQty * tenderEst.getCostUnitRate();
            tenderEst.setTotalCostCompanyCurrency(totalCost);
            tenderEst.setResourceTotalCost(totalCost * tenderEst.getExchangeRate());
        } else {
            InternalBoq parentIB = tenderEst.getParentInternalBoq();
            tenderEst = new TenderEstimation();
            tenderEst.setProject(project);
            tenderEst.setBoq(boq);
            tenderEst.setParent(parent);
            tenderEst.setParentInternalBoq(parentIB);
            updateTenderEstimationFields(tenderEst, dto);
            int maxRefCode;
            if (parent == null) {
                maxRefCode = tenderEstDao.findByBoqId(boq.getId()).stream()
                        .filter(e -> e.getParent() == null)
                        .mapToInt(TenderEstimation::getRefCode)
                        .max().orElse(0);
            } else {
                maxRefCode = tenderEstDao.findByParent_Id(parent.getId()).stream()
                        .mapToInt(TenderEstimation::getRefCode)
                        .max().orElse(0);
            }
            tenderEst.setRefCode(maxRefCode + 1);
            saveFormulaElements(tenderEst, dto);
            calculateValues(tenderEst, dto);
        }
        tenderEst = tenderEstDao.save(tenderEst);
        if (tenderEst.getParentInternalBoq() != null) {
            syncInternalBoqFromEstimation(tenderEst);
        }
        propagateCost(tenderEst);
        return "Saved Successfully";
    }

    @Transactional
    @Override
    public String updateResources(TenderResourceDto dto) {
        if (dto.getId() == null) throw new RuntimeException("Estimation ID missing for update");
        TenderEstimation tenderEst = tenderEstDao.findById(convertToUUID(dto.getId()))
                .orElseThrow(() -> new RuntimeException("TenderEstimation not found"));

        updateTenderEstimationFields(tenderEst, dto);
        saveFormulaElements(tenderEst, dto);
        calculateValues(tenderEst, dto);

        tenderEst = tenderEstDao.save(tenderEst);
        if (tenderEst.getParentInternalBoq() != null) {
            syncInternalBoqFromEstimation(tenderEst);
        }

        propagateCost(tenderEst);
        return "Updated Successfully";
    }

    private Optional<TenderEstimation> findDuplicateResource(
            TenderEstimation parent,
            BOQ boq,
            Projects project,
            UUID resourceId,
            UUID uomId,
            List<UUID> attributeIds,
            String natureId
    ) {
        List<TenderEstimation> candidates;
        if (ResourceNature.SIMPLE.name().equalsIgnoreCase(natureId)) {
            // For SIMPLE: check Resource + Attributes + BOQ + Project
            candidates = tenderEstDao.findByBoq_IdAndProject_IdAndResources_Id(
                    boq.getId(), project.getId(), resourceId
            );
        } else {
            // For COMPLEX or internal items: check Resource + Attributes + UOM within the current level
            if (parent != null && parent.getInternalBoq() != null) {
                candidates = tenderEstDao.findByParentInternalBoq_IdAndResources_IdAndUom_Id(
                        parent.getInternalBoq().getId(), resourceId, uomId);
            } else if (boq != null) {
                candidates = tenderEstDao.findByBoq_IdAndParentIsNullAndResources_IdAndUom_Id(
                        boq.getId(), resourceId, uomId);
            } else {
                candidates = new ArrayList<>();
            }
        }

        for (TenderEstimation est : candidates) {
            if (isSameTenderAttributes(est, attributeIds)) {
                return Optional.of(est);
            }
        }

        return Optional.empty();
    }

    private void syncInternalBoqFromEstimation(TenderEstimation est) {
        InternalBoq boq = est.getParentInternalBoq();
        // If the node itself is complex, it might own an InternalBoq that needs syncing too
        if (boq == null && est.getResourceNature() == ResourceNature.COMPLEX) {
             List<InternalBoq> owned = internalBoqDao.findByProject_IdAndResource_IdAndUom_Id(
                     est.getProject().getId(), est.getResources().getId(), est.getUom().getId());
             for (InternalBoq ib : owned) {
                 if (isSameInternalAttributes(ib, est.getEstimationAttributes())) {
                     boq = ib;
                     break;
                 }
             }
        }
        
        if (boq == null) return;
        
        // Items in breakup
        List<TenderEstimation> children = tenderEstDao.findByParentInternalBoq_Id(boq.getId());
        double totalCost = children.stream()
                .mapToDouble(TenderEstimation::getTotalCostCompanyCurrency)
                .sum();
        
        // Owners (Complex Resources sharing this setup)
        double totalOwnerQty = boq.getParentResource().stream()
                .mapToDouble(TenderEstimation::getNetQuantity)
                .sum();
        
        boq.setTotalQuantity(totalOwnerQty);
        boq.setAmount(totalCost);
        
        if (totalOwnerQty > 0) {
            boq.setRate(totalCost / totalOwnerQty);
        } else {
            boq.setRate(0.0);
        }
        internalBoqDao.save(boq);
    }

    private boolean isSameTenderAttributes(TenderEstimation est, List<UUID> attributeIds){
        List<TenderEstimationAttribute> existingAttrs = est.getEstimationAttributes();
        if (existingAttrs.size() != attributeIds.size()) return false;
        Set<UUID> existingIds = existingAttrs.stream()
                .map(a -> a.getAttribute().getId())
                .collect(Collectors.toSet());
        Set<UUID> incomingIds = new HashSet<>(attributeIds);
        return existingIds.equals(incomingIds);
    }

    private InternalBoq findOrCreateInternalBoq(TenderEstimation est) {
        List<InternalBoq> existingList = internalBoqDao
                .findByProject_IdAndResource_IdAndUom_Id(
                        est.getProject().getId(),
                        est.getResources().getId(),
                        est.getUom().getId()
                );
        for (InternalBoq boq : existingList) {
            if (isSameInternalAttributes(boq, est.getEstimationAttributes())) {
                if (!boq.getParentResource().contains(est)) {
                    boq.getParentResource().add(est);
                }
                est.setInternalBoq(boq);
                return boq;
            }
        }
        InternalBoq newBoq = new InternalBoq();
        newBoq.setProject(est.getProject());
        newBoq.setResource(est.getResources());
        newBoq.setUom(est.getUom());
        newBoq.setAttributeGroup(resolveAttributeGroup(est));
        List<InternalBoqAttribute> attrs = mapAttributes(est);
        for (InternalBoqAttribute attr : attrs) {
            attr.setInternalBoq(newBoq);
        }
        newBoq.setAttributes(attrs);
        newBoq.setTotalQuantity(est.getNetQuantity());
        newBoq.setAmount(est.getTotalCostCompanyCurrency());
        if (est.getNetQuantity() > 0) {
            newBoq.setRate(est.getTotalCostCompanyCurrency() / est.getNetQuantity());
        } else {
            newBoq.setRate(0.0);
        }
        newBoq.getParentResource().add(est);
        InternalBoq saved = internalBoqDao.save(newBoq);
        est.setInternalBoq(saved);
        return saved;
    }

    private AttributeGroup resolveAttributeGroup(TenderEstimation est) {
        if (est.getEstimationAttributes() == null || est.getEstimationAttributes().isEmpty()) {
            return null;
        }
        Set<AttributeGroup> groups = est.getEstimationAttributes().stream()
                .map(attr -> attr.getAttribute().getAttributeGroup())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (groups.isEmpty()) {
            return null;
        }
        if (groups.size() > 1) {
            throw new RuntimeException("Multiple AttributeGroups found for same TenderEstimation");
        }
        return groups.iterator().next();
    }

    private boolean isSameInternalAttributes(
            InternalBoq boq,
            List<TenderEstimationAttribute> attrs
    ) {
        if (boq.getAttributes().size() != attrs.size()) return false;

        Set<UUID> boqAttrIds = boq.getAttributes().stream()
                .map(a -> a.getAttribute().getId())
                .collect(Collectors.toSet());

        Set<UUID> incomingAttrIds = attrs.stream()
                .map(a -> a.getAttribute().getId())
                .collect(Collectors.toSet());

        return boqAttrIds.equals(incomingAttrIds);
    }

    private List<InternalBoqAttribute> mapAttributes(TenderEstimation est) {
        List<InternalBoqAttribute> list = new ArrayList<>();
        for (TenderEstimationAttribute attr : est.getEstimationAttributes()) {
            InternalBoqAttribute i = new InternalBoqAttribute();
            i.setAttribute(attr.getAttribute());
            list.add(i);
        }
        return list;
    }

    private void propagateCost(TenderEstimation node) {
        if (node.getResourceNature() == ResourceNature.COMPLEX) {
            InternalBoq iboq = findOrCreateInternalBoq(node);
            List<TenderEstimation> children = tenderEstDao.findByParentInternalBoq_Id(iboq.getId());
            double totalAmount = children.stream()
                    .mapToDouble(TenderEstimation::getTotalCostCompanyCurrency)
                    .sum();
            
            node.setTotalCostCompanyCurrency(totalAmount);
            if (node.getNetQuantity() > 0) {
                node.setCostUnitRate(totalAmount / node.getNetQuantity());
            } else {
                node.setCostUnitRate(0.0);
            }
            node.setResourceTotalCost(totalAmount * node.getExchangeRate());
            tenderEstDao.save(node);
            
            // Also sync the container object
            syncInternalBoqFromEstimation(node);
        }

        // 🔁 PROPAGATE UP via Parent
        if (node.getParent() != null) {
            propagateCost(node.getParent());
        } 
        
        // 🔁 PROPAGATE UP via InternalBoq Owners
        if (node.getParentInternalBoq() != null) {
            InternalBoq iboq = node.getParentInternalBoq();
            syncInternalBoqFromEstimation(node); // Ensure container is updated
            
            double newRate = iboq.getRate();
            for (TenderEstimation owner : iboq.getParentResource()) {
                owner.setCostUnitRate(newRate);
                owner.setTotalCostCompanyCurrency(owner.getNetQuantity() * newRate);
                owner.setResourceTotalCost(owner.getTotalCostCompanyCurrency() * owner.getExchangeRate());
                tenderEstDao.save(owner);
                
                // Recursively propagate from each owner
                propagateCost(owner);
            }
        }

        // 🔁 ROOT LEVEL (Final BOQ/Project updates)
        if (node.getParent() == null && node.getParentInternalBoq() == null) {
            if (node.getBoq() != null) {
                updateBoq(node.getBoq());
                updateProject(node.getBoq().getProject());
            }
        }
    }

    private void updateBoq(BOQ boq) {
        double total = tenderEstDao.findByBoqId(boq.getId()).stream()
                .filter(e -> e.getParent() == null)
                .mapToDouble(TenderEstimation::getTotalCostCompanyCurrency)
                .sum();
        boq.setTotalAmount(total);
        if (boq.getQuantity() > 0) {
            boq.setTotalRate(total / boq.getQuantity());
        }
        boqDao.save(boq);
    }

    private void updateProject(Projects project) {
        List<BOQ> boqList = boqDao.findByProjectId(project.getId());
        double total = boqList.stream()
                .mapToDouble(BOQ::getTotalAmount)
                .sum();
        project.setTotalEstimatedValue(BigDecimal.valueOf(total));
        projectDao.save(project);
    }

    private void calculateValues(TenderEstimation t, TenderResourceDto dto) {

        double calculatedQty;

        if (t.getParent() == null) {
            if (t.getQuantityType() == QuantityType.FORMULA) {
                calculatedQty = evaluateFormula(t.getFormulaElements());
            } else if (t.getQuantityType() == QuantityType.DIRECT) {
                calculatedQty = t.getBoq().getQuantity();
            } else {
                calculatedQty = t.getBoq().getQuantity() * t.getCoEfficient();
            }
        } else {
            calculatedQty = t.getQuantityType() == QuantityType.FORMULA
                    ? evaluateFormula(t.getFormulaElements())
                    : t.getCoEfficient();
        }

        double wasteQty = (dto.getWastePercentage() / 100) * calculatedQty;
        double netQty = calculatedQty + wasteQty;

        double totalCost = netQty * dto.getCostUnitRate();

        t.setCalculatedQuantity(calculatedQty);
        t.setWasteQuantity(wasteQty);
        t.setNetQuantity(netQty);
        t.setTotalCostCompanyCurrency(totalCost);
        t.setResourceTotalCost(totalCost * dto.getExchangeRate());
    }

    private double evaluateFormula(List<TenderFormulaElement> elements) {
        if (elements == null || elements.isEmpty()) return 0;
        elements.sort(Comparator.comparingInt(TenderFormulaElement::getSequence));
        List<Double> values = new ArrayList<>();
        List<String> operators = new ArrayList<>();
        for (TenderFormulaElement el : elements) {
            switch (el.getType()) {
                case SOURCE:
                    double value = 0;
                    if (el.getSourceType() == FormulaSourceType.BOQ && el.getBoq() != null) {
                        value = el.getBoq().getQuantity();
                    } else if (el.getSourceType() == FormulaSourceType.RESOURCE && el.getResource() != null) {
                        value = el.getResource().getCalculatedQuantity();
                    } else if (el.getSourceType() == FormulaSourceType.GLOBAL && el.getGlobalValue() != null) {
                        value = el.getGlobalValue().getValue();
                    }
                    values.add(value);
                    break;
                case NUMBER:
                    values.add(el.getValue());
                    break;
                case OPERATOR:
                    operators.add(el.getOperator());
                    break;
            }
        }
        double result = values.get(0);
        for (int i = 0; i < operators.size(); i++) {
            String op = operators.get(i);
            double next = values.get(i + 1);
            switch (op) {
                case "+": result += next; break;
                case "-": result -= next; break;
                case "*": result *= next; break;
                case "/": result = next != 0 ? result / next : 0; break;
            }
        }
        return result;
    }

    private void saveFormulaElements(TenderEstimation tenderEst, TenderResourceDto dto) {
        tenderEst.getFormulaElements().clear();
        if (dto.getFormula() == null || dto.getFormula().isEmpty()) {
            return;
        }
        for (FormulaElementDto f : dto.getFormula()) {
            TenderFormulaElement el = new TenderFormulaElement();
            el.setTenderEstimation(tenderEst);
            el.setSequence(f.getSequence());
            el.setType(FormulaElementType.valueOf(f.getType()));
            el.setSourceType(
                    f.getSourceType() != null
                            ? FormulaSourceType.valueOf(f.getSourceType())
                            : null
            );
            if ("BOQ".equalsIgnoreCase(f.getSourceType())) {
                el.setBoq(boqDao.findById(convertToUUID(f.getRefId()))
                        .orElseThrow(() -> new RuntimeException("BOQ not found")));
            }
            else if ("RESOURCE".equalsIgnoreCase(f.getSourceType())) {
                el.setResource(tenderEstDao.findById(convertToUUID(f.getRefId()))
                        .orElseThrow(() -> new RuntimeException("Resource not found")));
            }
            else if ("GLOBAL".equalsIgnoreCase(f.getSourceType())) {
                el.setGlobalValue(globalValueDao.findById(convertToUUID(f.getRefId()))
                        .orElseThrow(() -> new RuntimeException("Global value not found")));
            }
            if ("OPERATOR".equalsIgnoreCase(f.getType())) {
                el.setOperator(f.getOperator());
            } else {
                el.setValue(f.getValue());
            }
            tenderEst.getFormulaElements().add(el);
        }
    }

    private void updateTenderEstimationFields(TenderEstimation tenderEst, TenderResourceDto dto) {
        if (dto.isInternal()) {
            String targetId = (dto.getInternalBoqId() != null && !dto.getInternalBoqId().isEmpty()) 
                    ? dto.getInternalBoqId() : dto.getBoqId();
            InternalBoq parentBoq = internalBoqDao.findById(convertToUUID(targetId)).orElse(null);
            tenderEst.setParentInternalBoq(parentBoq);
        } else {
            tenderEst.setParentInternalBoq(null);
        }
        tenderEst.setDocNumber(dto.getDocNumber());
        tenderEst.setCoEfficient(dto.getCoEfficient());
        tenderEst.setWastePercentage(dto.getWastePercentage());
        tenderEst.setRate(dto.getRate());
        tenderEst.setAdditionalRate(dto.getAdditionalRate());
        tenderEst.setShippingPrice(dto.getShippingPrice());
        tenderEst.setCostUnitRate(dto.getCostUnitRate());
        tenderEst.setRateLock(dto.isRateLock());
        tenderEst.setExchangeRate(dto.getExchangeRate());

        ResourceType resourceType = (dto.getResourceTypeId() != null && !dto.getResourceTypeId().isEmpty())
                ? ResourceType.valueOf(dto.getResourceTypeId())
                : null;
        tenderEst.setResourceType(resourceType);

        Projects project = (dto.getProjectId() != null && !dto.getProjectId().trim().isEmpty() )
                ? projectDao.findById(convertToUUID(dto.getProjectId())).orElse(null)
                : null;
        tenderEst.setProject(project);
        Resources resource = (dto.getResourceId() != null && !dto.getResourceId().trim().isEmpty())
                ? masterService.findResourceById(dto.getResourceId())
                : null;
        tenderEst.setResources(resource);
        Uom uom = (dto.getUomId() != null && !dto.getUomId().trim().isEmpty())
                ? masterService.findUomById(dto.getUomId())
                : null;
        tenderEst.setUom(uom);
        ResourceNature resourceNature = (dto.getResourceNatureId() != null && !dto.getResourceNatureId().isEmpty())
                ? ResourceNature.valueOf(dto.getResourceNatureId().toUpperCase())
                : null;
        tenderEst.setResourceNature(resourceNature);
        QuantityType quantityType = null;
        if (dto.getQuantityTypeId() != null && !dto.getQuantityTypeId().trim().isEmpty()) {
            quantityType = QuantityType.valueOf(dto.getQuantityTypeId().toUpperCase());
        }
        tenderEst.setQuantityType(quantityType);
        Currency currency = (dto.getCurrencyId() != null && !dto.getCurrencyId().trim().isEmpty())
                ? masterService.findCurrencyById(dto.getCurrencyId())
                : null;
        tenderEst.setCurrency(currency);
        if (dto.getAttributes() != null && !dto.getAttributes().isEmpty()) {
            tenderEst.getEstimationAttributes().clear();
            List<TenderEstimationAttribute> attributes = new ArrayList<>();
            for (AttributeSelectionDto d : dto.getAttributes()) {
                TenderEstimationAttribute attribute = new TenderEstimationAttribute();
                attribute.setTenderEstimation(tenderEst);
                attribute.setAttributeGroup(
                        attributeGroupDao.findById(convertToUUID(d.getAttributeGroupId()))
                                .orElseThrow(() -> new RuntimeException("Attribute Group not found"))
                );
                attribute.setAttribute(
                        attributeDao.findById(convertToUUID(d.getAttributeId()))
                                .orElseThrow(() -> new RuntimeException("Attribute not found"))
                );
                attributes.add(attribute);
            }
            tenderEst.setEstimationAttributes(attributes);
        }
    }

    @Override
    public List<InternalBoq> getInternalBoqByProject(UUID projectId) {
        Projects project = projectDao.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return internalBoqDao.findByProject(project);
    }

    @Override
    public List<TenderEstResourceDto> getEstimatedResourcesByBoq(String boqId) {
        UUID boqUUID = convertToUUID(boqId);
        List<TenderEstimation> estimations = tenderEstDao.findByBoqId(boqUUID);
        List<TenderEstResourceDto> dtoList = new ArrayList<>();
        for (TenderEstimation est : estimations) {
            TenderEstResourceDto dto = new TenderEstResourceDto();
            dto.setTenderEstimation(est);
            dto.setCalculatedQuantity(est.getCalculatedQuantity());
            dto.setWasteQuantity(est.getWasteQuantity());
            dto.setNetQuantity(est.getNetQuantity());
            dto.setTotalCostCompanyCurrency(est.getTotalCostCompanyCurrency());
            dto.setResourceTotalCost(est.getResourceTotalCost());
            dto.setCoEfficient(est.getCoEfficient());
            if (est.getResourceNature() == ResourceNature.COMPLEX) {
                InternalBoq iboq = findOrCreateInternalBoq(est);
                if (iboq != null) {
                    dto.setInternalBoqId(iboq.getId().toString());
                }
            }
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    public List<TenderEstResourceDto> getEstimatedResourcesByInternalBoq(String internalBoqId) {
        UUID ibUUID = convertToUUID(internalBoqId);
        List<TenderEstimation> estimations = tenderEstDao.findByParentInternalBoq_Id(ibUUID);
        List<TenderEstResourceDto> dtoList = new ArrayList<>();
        for (TenderEstimation est : estimations) {
            TenderEstResourceDto dto = new TenderEstResourceDto();
            dto.setTenderEstimation(est);
            dto.setCalculatedQuantity(est.getCalculatedQuantity());
            dto.setWasteQuantity(est.getWasteQuantity());
            dto.setNetQuantity(est.getNetQuantity());
            dto.setTotalCostCompanyCurrency(est.getTotalCostCompanyCurrency());
            dto.setResourceTotalCost(est.getResourceTotalCost());
            dto.setCoEfficient(est.getCoEfficient());
            if (est.getResourceNature() == ResourceNature.COMPLEX) {
                InternalBoq iboq = findOrCreateInternalBoq(est);
                if (iboq != null) {
                    dto.setInternalBoqId(iboq.getId().toString());
                }
            }
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    public InternalBoq getInternalBoqById(String internalBoqId) {
        return  internalBoqDao.findById(convertToUUID(internalBoqId))
                .orElseThrow(()->new DataNotFoundException(
                        messageSource.getMessage("data.not.found",null, Locale.ENGLISH)));
    }

    @Override
    @Transactional
    public String deleteTenderEstimationForBoq(String tenderEstimationId, String boqId) {
        UUID tenderId = convertToUUID(tenderEstimationId);
        TenderEstimation tenderEstimation = tenderEstDao.findById(tenderId)
                .orElseThrow(() -> new RuntimeException("TenderEstimation not found"));
        
        BOQ boq = tenderEstimation.getBoq();
        TenderEstimation parent = tenderEstimation.getParent();

        // 1. Recursive delete of children
        List<TenderEstimation> children = tenderEstDao.findByParent_Id(tenderEstimation.getId());
        for (TenderEstimation child : children) {
            deleteTenderEstimationForBoq(child.getId().toString(), boqId);
        }

        // 2. Identify and cleanup InternalBoq it might be part of (as child or parent)
        // We find all InternalBoqs that might be linked to this specific estimation record.
        // A direct way is to check all InternalBoqs that have this estimation in their parentResource list,
        // AND check the internalBoq field itself.
        
        Set<InternalBoq> relatedBoqs = new HashSet<>();
        if (tenderEstimation.getInternalBoq() != null) {
            relatedBoqs.add(tenderEstimation.getInternalBoq());
        }
        if (tenderEstimation.getParentInternalBoq() != null) {
            relatedBoqs.add(tenderEstimation.getParentInternalBoq());
        }

        for (InternalBoq ib : relatedBoqs) {
            cleanupInternalBoq(ib, tenderEstimation.getId());
        }

        // 3. Delete the estimation
        tenderEstDao.delete(tenderEstimation);

        // 4. Propagate
        if (parent != null) {
            propagateCost(parent);
        } else if (boq != null) {
            updateBoq(boq);
            updateProject(boq.getProject());
        }
        return messageSource.getMessage("tender.resource.delete.success", null, Locale.ENGLISH);
    }

    private void cleanupInternalBoq(InternalBoq iBoq, UUID currentEstId) {
        if (iBoq == null) return;
        
        // Remove the estimation from its parentResource list
        iBoq.getParentResource().removeIf(est -> est.getId().equals(currentEstId));
        
        // Check if any OTHER estimations still reference this iBoq as container
        List<TenderEstimation> children = tenderEstDao.findByParentInternalBoq_Id(iBoq.getId());
        long remainingChildren = children.stream()
                .filter(c -> !c.getId().equals(currentEstId))
                .count();
        
        if (iBoq.getParentResource().isEmpty() && remainingChildren == 0) {
            internalBoqDao.delete(iBoq);
        } else {
            // Update totals based on remaining members
            double totalQty = iBoq.getParentResource().stream()
                    .mapToDouble(TenderEstimation::getNetQuantity)
                    .sum();
            double totalAmount = iBoq.getParentResource().stream()
                    .mapToDouble(TenderEstimation::getTotalCostCompanyCurrency)
                    .sum();
            iBoq.setTotalQuantity(totalQty);
            iBoq.setAmount(totalAmount);
            if (totalQty > 0) {
                iBoq.setRate(totalAmount / totalQty);
            } else {
                iBoq.setRate(0.0);
            }
            internalBoqDao.save(iBoq);
        }
    }

    @Override
    public TenderEstResourceDto getEstimatedResourceById(String tenderEstimationId) {
        UUID tenderId = convertToUUID(tenderEstimationId);
        TenderEstimation tenderEstimation = tenderEstDao.findById(tenderId)
                .orElseThrow(() -> new RuntimeException(
                        "TenderEstimation not found with ID: " + tenderEstimationId));
        TenderEstResourceDto dto = new TenderEstResourceDto();
        dto.setTenderEstimation(tenderEstimation);
        dto.setCalculatedQuantity(tenderEstimation.getCalculatedQuantity());
        dto.setWasteQuantity(tenderEstimation.getWasteQuantity());
        dto.setNetQuantity(tenderEstimation.getNetQuantity());
        dto.setTotalCostCompanyCurrency(tenderEstimation.getTotalCostCompanyCurrency());
        dto.setResourceTotalCost(tenderEstimation.getResourceTotalCost());
        dto.setCoEfficient(tenderEstimation.getCoEfficient());
        return dto;
    }

    @Override
    public TenderEstResourceDto getEstimatedResourceByIdAndBoqId(String tenderEstimationId, String boqId) {
        UUID tenderId = convertToUUID(tenderEstimationId);
        UUID boqUUID = convertToUUID(boqId);
        TenderEstimation tenderEstimation = tenderEstDao.findById(tenderId)
                .orElseThrow(() -> new RuntimeException(
                        "TenderEstimation not found with ID: " + tenderEstimationId));
        if (tenderEstimation.getBoq() == null ||
                !tenderEstimation.getBoq().getId().equals(boqUUID)) {
            throw new RuntimeException(
                    "TenderEstimation does not belong to BOQ: " + boqId);
        }
        TenderEstResourceDto dto = new TenderEstResourceDto();
        dto.setTenderEstimation(tenderEstimation);
        dto.setCalculatedQuantity(tenderEstimation.getCalculatedQuantity());
        dto.setWasteQuantity(tenderEstimation.getWasteQuantity());
        dto.setNetQuantity(tenderEstimation.getNetQuantity());
        dto.setTotalCostCompanyCurrency(tenderEstimation.getTotalCostCompanyCurrency());
        dto.setResourceTotalCost(tenderEstimation.getResourceTotalCost());
        dto.setCoEfficient(tenderEstimation.getCoEfficient());
        return dto;
    }

    @Override
    public String addGlobalValue(String projectId, GlobalValue globalValue) {
        UUID projId = convertToUUID(projectId);
        if (globalValueDao.existsByProject_IdAndNameIgnoreCase(projId, globalValue.getName())) {
            throw new RuntimeException("Global Value name already exists for this project");
        }
        if (globalValueDao.existsByProject_IdAndCodeIgnoreCase(projId, globalValue.getCode())) {
            throw new RuntimeException("Global Value code already exists for this project");
        }
        Projects project = projectDao.findById(projId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        globalValue.setProject(project);
        globalValueDao.save(globalValue);
        return "Global Value added successfully";
    }

    @Override
    public List<GlobalValue> viewAllGlobalValueByProjectId(String projectId) {
        return globalValueDao.findByProject_Id(convertToUUID(projectId));
    }

    @Override
    public String editGlobalValue(String projectId, GlobalValue globalValue) {
        UUID projId = convertToUUID(projectId);
        GlobalValue existing = globalValueDao.findById(globalValue.getId())
                .orElseThrow(() -> new RuntimeException("Global Value not found"));
        if (globalValueDao.existsByProject_IdAndNameIgnoreCaseAndIdNot(
                projId, globalValue.getName(), globalValue.getId())) {
            throw new RuntimeException("Global Value name already exists for this project");
        }
        if (globalValueDao.existsByProject_IdAndCodeIgnoreCaseAndIdNot(
                projId, globalValue.getCode(), globalValue.getId())) {
            throw new RuntimeException("Global Value code already exists for this project");
        }
        existing.setName(globalValue.getName());
        existing.setCode(globalValue.getCode());
        existing.setValue(globalValue.getValue());
        globalValueDao.save(existing);
        return "Global Value updated successfully";
    }
}
