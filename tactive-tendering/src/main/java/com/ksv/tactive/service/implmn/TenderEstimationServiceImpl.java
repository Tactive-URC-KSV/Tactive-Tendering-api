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
        if (dto.getBoqId() == null && dto.getInternalBoqId() == null) throw new RuntimeException("BOQ Id or Internal BOQ Id missing");

        Projects project = projectDao.findById(convertToUUID(dto.getProjectId()))
                .orElseThrow(() -> new RuntimeException("Project not found"));

        TenderEstimation tenderEst = new TenderEstimation();
        List<TenderEstimation> parents = new ArrayList<>();
        BOQ boq = null;
        InternalBoq parentInternalBoq = null;

        boolean isSimple = ResourceNature.SIMPLE.name().equalsIgnoreCase(dto.getResourceNatureId());

        if (!isSimple && dto.getInternalBoqId() != null && !dto.getInternalBoqId().isEmpty()) {
            parentInternalBoq = internalBoqDao.findById(convertToUUID(dto.getInternalBoqId()))
                    .orElseThrow(() -> new RuntimeException("Internal BOQ not found"));

            tenderEst.setParentInternalBoq(parentInternalBoq);
            tenderEst.setProject(project);
            tenderEst.setBoq(null); // Explicitly don't set BOQ if Internal BOQ is present

            if (parentInternalBoq.getParentResource() != null && !parentInternalBoq.getParentResource().isEmpty()) {
                parents = new ArrayList<>(parentInternalBoq.getParentResource());
            }
        } else {
            String targetBoqId = dto.getBoqId();

            if ((targetBoqId == null || targetBoqId.isEmpty()) && dto.getInternalBoqId() != null) {
                InternalBoq tempIb = internalBoqDao.findById(convertToUUID(dto.getInternalBoqId())).orElse(null);
                if (tempIb != null && !tempIb.getParentResource().isEmpty() && tempIb.getParentResource().get(0).getBoq() != null) {
                    targetBoqId = tempIb.getParentResource().get(0).getBoq().getId().toString();
                }
            }

            if (targetBoqId == null || targetBoqId.isEmpty()) {
                throw new RuntimeException("BOQ Id missing and could not be derived for Simple Resource");
            }

            boq = boqDao.findById(convertToUUID(targetBoqId))
                    .orElseThrow(() -> new RuntimeException("BOQ not found"));

            tenderEst.setProject(project);
            tenderEst.setBoq(boq);
            tenderEst.setParentInternalBoq(null);
            tenderEst.setParent(new ArrayList<>());
        }

        UUID resourceId = convertToUUID(dto.getResourceId());
        UUID uomId = convertToUUID(dto.getUomId());
        List<UUID> attributeIds = dto.getAttributes() != null
                ? dto.getAttributes().stream().map(a -> convertToUUID(a.getAttributeId())).toList()
                : new ArrayList<>();

        Optional<TenderEstimation> duplicateOpt = findDuplicateResource(
                parentInternalBoq, boq, project, resourceId, uomId, attributeIds, dto.getResourceNatureId()
        );

        if (duplicateOpt.isPresent()) {
            tenderEst = duplicateOpt.get();
            if (isSimple) {
                throw new DuplicateEntryException("This simple resource already exists for this BOQ and Project.");
            }
            double newQty = tenderEst.getNetQuantity() + dto.getNetQuantity();
            tenderEst.setNetQuantity(newQty);
            double totalCost = newQty * tenderEst.getCostUnitRate();
            tenderEst.setTotalCostCompanyCurrency(totalCost);
            tenderEst.setResourceTotalCost(totalCost * tenderEst.getExchangeRate());
        } else {
            tenderEst = new TenderEstimation();
            tenderEst.setProject(project);
            tenderEst.setBoq(boq); // Will be null if it's an Internal BOQ item
            tenderEst.setParent(parents);
            tenderEst.setParentInternalBoq(parentInternalBoq);

            updateTenderEstimationFields(tenderEst, dto);

            int maxRefCode;
            if (parents.isEmpty() && boq != null) {
                maxRefCode = tenderEstDao.findByBoqId(boq.getId()).stream()
                        .filter(e -> e.getParent() == null || e.getParent().isEmpty())
                        .mapToInt(TenderEstimation::getRefCode)
                        .max().orElse(0);
            } else if (!parents.isEmpty()) {
                maxRefCode = tenderEstDao.findByParent_Id(parents.get(0).getId()).stream()
                        .mapToInt(TenderEstimation::getRefCode)
                        .max().orElse(0);
            } else {
                maxRefCode = 0;
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
            InternalBoq containerIB, BOQ boq, Projects project, UUID resourceId, UUID uomId, List<UUID> attributeIds, String natureId
    ) {
        List<TenderEstimation> candidates;
        if (ResourceNature.SIMPLE.name().equalsIgnoreCase(natureId)) {
            candidates = tenderEstDao.findByBoq_IdAndProject_IdAndResources_Id(
                    boq.getId(), project.getId(), resourceId
            );
        } else {
            if (containerIB != null) {
                candidates = tenderEstDao.findByParentInternalBoq_IdAndResources_IdAndUom_Id(
                        containerIB.getId(), resourceId, uomId);
            } else if (boq != null) {
                candidates = tenderEstDao.findByBoq_IdAndParentIsNullAndResources_IdAndUom_Id(
                        boq.getId(), resourceId, uomId);
            } else {
                candidates = new ArrayList<>();
            }
        }

        for (TenderEstimation est : candidates) {
            if (isSameTenderAttributes(est, attributeIds)) return Optional.of(est);
        }

        return Optional.empty();
    }

    private void syncInternalBoqFromEstimation(TenderEstimation est) {
        InternalBoq boq = est.getParentInternalBoq();
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

        List<TenderEstimation> children = tenderEstDao.findByParentInternalBoq_Id(boq.getId());

        double unitRateOfComplexItem = children.stream()
                .mapToDouble(TenderEstimation::getTotalCostCompanyCurrency)
                .sum();

        double totalOwnerQty = boq.getParentResource().stream()
                .mapToDouble(TenderEstimation::getNetQuantity)
                .sum();

        boq.setTotalQuantity(totalOwnerQty);
        boq.setRate(unitRateOfComplexItem);
        boq.setAmount(totalOwnerQty * unitRateOfComplexItem);

        internalBoqDao.save(boq);
    }

    private boolean isSameTenderAttributes(TenderEstimation est, List<UUID> attributeIds){
        List<TenderEstimationAttribute> existingAttrs = est.getEstimationAttributes();
        if (existingAttrs.size() != attributeIds.size()) return false;
        Set<UUID> existingIds = existingAttrs.stream().map(a -> a.getAttribute().getId()).collect(Collectors.toSet());
        Set<UUID> incomingIds = new HashSet<>(attributeIds);
        return existingIds.equals(incomingIds);
    }

    private InternalBoq findOrCreateInternalBoq(TenderEstimation est) {
        List<InternalBoq> existingList = internalBoqDao
                .findByProject_IdAndResource_IdAndUom_Id(est.getProject().getId(), est.getResources().getId(), est.getUom().getId());
        for (InternalBoq boq : existingList) {
            if (isSameInternalAttributes(boq, est.getEstimationAttributes())) {
                if (!boq.getParentResource().contains(est)) boq.getParentResource().add(est);
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
        newBoq.setRate(0.0);
        newBoq.setAmount(0.0);
        newBoq.getParentResource().add(est);
        return internalBoqDao.save(newBoq);
    }

    private AttributeGroup resolveAttributeGroup(TenderEstimation est) {
        if (est.getEstimationAttributes() == null || est.getEstimationAttributes().isEmpty()) return null;
        Set<AttributeGroup> groups = est.getEstimationAttributes().stream()
                .map(attr -> attr.getAttribute().getAttributeGroup())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (groups.isEmpty()) return null;
        if (groups.size() > 1) throw new RuntimeException("Multiple AttributeGroups found for same TenderEstimation");
        return groups.iterator().next();
    }

    private boolean isSameInternalAttributes(InternalBoq boq, List<TenderEstimationAttribute> attrs) {
        if (boq.getAttributes().size() != attrs.size()) return false;
        Set<UUID> boqAttrIds = boq.getAttributes().stream().map(a -> a.getAttribute().getId()).collect(Collectors.toSet());
        Set<UUID> incomingAttrIds = attrs.stream().map(a -> a.getAttribute().getId()).collect(Collectors.toSet());
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
            double unitRate = children.stream()
                    .mapToDouble(TenderEstimation::getTotalCostCompanyCurrency)
                    .sum();

            node.setCostUnitRate(unitRate);
            node.setTotalCostCompanyCurrency(node.getNetQuantity() * unitRate);
            node.setResourceTotalCost(node.getTotalCostCompanyCurrency() * node.getExchangeRate());
            tenderEstDao.save(node);

            syncInternalBoqFromEstimation(node);
        }

        if (node.getParent() != null && !node.getParent().isEmpty()) {
            for (TenderEstimation parent : node.getParent()) {
                propagateCost(parent);
            }
        }

        if ((node.getParent() == null || node.getParent().isEmpty()) && node.getParentInternalBoq() == null) {
            if (node.getBoq() != null) {
                updateBoq(node.getBoq());
                updateProject(node.getBoq().getProject());
            }
        }
    }

    private void updateBoq(BOQ boq) {
        double total = tenderEstDao.findByBoqId(boq.getId()).stream()
                .filter(e -> e.getParent() == null || e.getParent().isEmpty())
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
        double unitMultiplier = 0.0;

        if (t.getQuantityType() == QuantityType.FORMULA) {
            unitMultiplier = evaluateFormula(t.getFormulaElements());
        } else {
            unitMultiplier = t.getCoEfficient();
        }

        if (t.getParentInternalBoq() != null) {
            calculatedQty = unitMultiplier;
        } else {
            // Root level items
            if (t.getQuantityType() == QuantityType.FORMULA) {
                // Strictly use the formula result, ignoring BOQ quantity
                calculatedQty = unitMultiplier;
            } else if (t.getQuantityType() == QuantityType.DIRECT) {
                // Direct uses exact BOQ quantity
                calculatedQty = t.getBoq() != null ? t.getBoq().getQuantity() : 0.0;
            } else {
                // Coefficient multiplies the BOQ quantity by the coefficient
                calculatedQty = (t.getBoq() != null ? t.getBoq().getQuantity() : 0.0) * unitMultiplier;
            }
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
        if (dto.getFormula() == null || dto.getFormula().isEmpty()) return;

        for (FormulaElementDto f : dto.getFormula()) {
            TenderFormulaElement el = new TenderFormulaElement();
            el.setTenderEstimation(tenderEst);
            el.setSequence(f.getSequence());
            el.setType(FormulaElementType.valueOf(f.getType()));
            el.setSourceType(f.getSourceType() != null ? FormulaSourceType.valueOf(f.getSourceType()) : null);

            if ("BOQ".equalsIgnoreCase(f.getSourceType())) {
                el.setBoq(boqDao.findById(convertToUUID(f.getRefId()))
                        .orElseThrow(() -> new RuntimeException("BOQ not found")));
            } else if ("RESOURCE".equalsIgnoreCase(f.getSourceType())) {
                el.setResource(tenderEstDao.findById(convertToUUID(f.getRefId()))
                        .orElseThrow(() -> new RuntimeException("Resource not found")));
            } else if ("GLOBAL".equalsIgnoreCase(f.getSourceType())) {
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
        boolean isSimple = ResourceNature.SIMPLE.name().equalsIgnoreCase(dto.getResourceNatureId());

        if (!isSimple && dto.getInternalBoqId() != null && !dto.getInternalBoqId().isEmpty()) {
            InternalBoq parentBoq = internalBoqDao.findById(convertToUUID(dto.getInternalBoqId())).orElse(null);
            tenderEst.setParentInternalBoq(parentBoq);
            tenderEst.setBoq(null); // Explicitly don't set BOQ if Internal BOQ is present
            if (parentBoq != null && parentBoq.getParentResource() != null) {
                tenderEst.setParent(new ArrayList<>(parentBoq.getParentResource()));
            }
        } else {
            tenderEst.setParentInternalBoq(null);
            tenderEst.setParent(new ArrayList<>());
            // Note: If falling back to standard BOQ processing, ensure boq assignment happens correctly in addResourcesToTender context.
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

        tenderEst.setResourceType(dto.getResourceTypeId() != null && !dto.getResourceTypeId().isEmpty()
                ? ResourceType.valueOf(dto.getResourceTypeId()) : null);

        tenderEst.setProject(dto.getProjectId() != null && !dto.getProjectId().trim().isEmpty()
                ? projectDao.findById(convertToUUID(dto.getProjectId())).orElse(null) : null);

        tenderEst.setResources(dto.getResourceId() != null && !dto.getResourceId().trim().isEmpty()
                ? masterService.findResourceById(dto.getResourceId()) : null);

        tenderEst.setUom(dto.getUomId() != null && !dto.getUomId().trim().isEmpty()
                ? masterService.findUomById(dto.getUomId()) : null);

        tenderEst.setResourceNature(dto.getResourceNatureId() != null && !dto.getResourceNatureId().isEmpty()
                ? ResourceNature.valueOf(dto.getResourceNatureId().toUpperCase()) : null);

        tenderEst.setQuantityType(dto.getQuantityTypeId() != null && !dto.getQuantityTypeId().trim().isEmpty()
                ? QuantityType.valueOf(dto.getQuantityTypeId().toUpperCase()) : null);

        tenderEst.setCurrency(dto.getCurrencyId() != null && !dto.getCurrencyId().trim().isEmpty()
                ? masterService.findCurrencyById(dto.getCurrencyId()) : null);

        if (dto.getAttributes() != null && !dto.getAttributes().isEmpty()) {
            tenderEst.getEstimationAttributes().clear();
            List<TenderEstimationAttribute> attributes = new ArrayList<>();
            for (AttributeSelectionDto d : dto.getAttributes()) {
                TenderEstimationAttribute attribute = new TenderEstimationAttribute();
                attribute.setTenderEstimation(tenderEst);
                attribute.setAttributeGroup(attributeGroupDao.findById(convertToUUID(d.getAttributeGroupId()))
                        .orElseThrow(() -> new RuntimeException("Attribute Group not found")));
                attribute.setAttribute(attributeDao.findById(convertToUUID(d.getAttributeId()))
                        .orElseThrow(() -> new RuntimeException("Attribute not found")));
                attributes.add(attribute);
            }
            tenderEst.setEstimationAttributes(attributes);
        }
    }

    @Override
    public List<InternalBoq> getInternalBoqByProject(UUID projectId) {
        Projects project = projectDao.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        return internalBoqDao.findByProject(project);
    }

    @Override
    public List<TenderEstResourceDto> getEstimatedResourcesByBoq(String boqId) {
        List<TenderEstimation> estimations = tenderEstDao.findByBoqId(convertToUUID(boqId));
        return buildDtoList(estimations);
    }

    @Override
    public List<TenderEstResourceDto> getEstimatedResourcesByInternalBoq(String internalBoqId) {
        List<TenderEstimation> estimations = tenderEstDao.findByParentInternalBoq_Id(convertToUUID(internalBoqId));
        return buildDtoList(estimations);
    }

    private List<TenderEstResourceDto> buildDtoList(List<TenderEstimation> estimations) {
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
                if (iboq != null) dto.setInternalBoqId(iboq.getId().toString());
            }
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    public InternalBoq getInternalBoqById(String internalBoqId) {
        return internalBoqDao.findById(convertToUUID(internalBoqId))
                .orElseThrow(()->new DataNotFoundException(messageSource.getMessage("data.not.found",null, Locale.ENGLISH)));
    }

    @Override
    @Transactional
    public String deleteTenderEstimationForBoq(String tenderEstimationId, String boqId) {
        TenderEstimation tenderEstimation = tenderEstDao.findById(convertToUUID(tenderEstimationId))
                .orElseThrow(() -> new RuntimeException("TenderEstimation not found"));

        BOQ boq = tenderEstimation.getBoq();
        List<TenderEstimation> parents = new ArrayList<>(tenderEstimation.getParent());

        // 1. Recursive delete of children
        List<TenderEstimation> children = tenderEstDao.findByParent_Id(tenderEstimation.getId());
        for (TenderEstimation child : children) {
            deleteTenderEstimationForBoq(child.getId().toString(), boqId);
        }

        // 2. Identify and cleanup InternalBoq
        Set<InternalBoq> relatedBoqs = new HashSet<>();
        if (tenderEstimation.getParentInternalBoq() != null) {
            relatedBoqs.add(tenderEstimation.getParentInternalBoq());
        }
        for (InternalBoq ib : relatedBoqs) {
            cleanupInternalBoq(ib, tenderEstimation.getId());
        }

        // 3. Delete the estimation
        tenderEstDao.delete(tenderEstimation);

        // 4. Recalculate up the chain
        if (!parents.isEmpty()) {
            for (TenderEstimation parent : parents) {
                propagateCost(parent);
            }
        } else if (boq != null) {
            updateBoq(boq);
            updateProject(boq.getProject());
        }

        return messageSource.getMessage("tender.resource.delete.success", null, Locale.ENGLISH);
    }

    private void cleanupInternalBoq(InternalBoq iBoq, UUID currentEstId) {
        if (iBoq == null) return;
        iBoq.getParentResource().removeIf(est -> est.getId().equals(currentEstId));

        List<TenderEstimation> children = tenderEstDao.findByParentInternalBoq_Id(iBoq.getId());
        long remainingChildren = children.stream().filter(c -> !c.getId().equals(currentEstId)).count();

        if (iBoq.getParentResource().isEmpty() && remainingChildren == 0) {
            internalBoqDao.delete(iBoq);
        } else {
            double totalQty = iBoq.getParentResource().stream().mapToDouble(TenderEstimation::getNetQuantity).sum();

            // Recalculate Unit Rate excluding the deleted item
            double unitRate = children.stream().filter(c -> !c.getId().equals(currentEstId))
                    .mapToDouble(TenderEstimation::getTotalCostCompanyCurrency).sum();

            iBoq.setTotalQuantity(totalQty);
            iBoq.setRate(unitRate);
            iBoq.setAmount(totalQty * unitRate);
            internalBoqDao.save(iBoq);
        }
    }

    @Override
    public TenderEstResourceDto getEstimatedResourceById(String tenderEstimationId) {
        TenderEstimation tenderEstimation = tenderEstDao.findById(convertToUUID(tenderEstimationId))
                .orElseThrow(() -> new RuntimeException("TenderEstimation not found with ID: " + tenderEstimationId));
        return buildSingleDto(tenderEstimation);
    }

    @Override
    public TenderEstResourceDto getEstimatedResourceByIdAndBoqId(String tenderEstimationId, String boqId) {
        TenderEstimation tenderEstimation = tenderEstDao.findById(convertToUUID(tenderEstimationId))
                .orElseThrow(() -> new RuntimeException("TenderEstimation not found with ID: " + tenderEstimationId));
        if (tenderEstimation.getBoq() == null || !tenderEstimation.getBoq().getId().equals(convertToUUID(boqId))) {
            throw new RuntimeException("TenderEstimation does not belong to BOQ: " + boqId);
        }
        return buildSingleDto(tenderEstimation);
    }

    private TenderEstResourceDto buildSingleDto(TenderEstimation tenderEstimation) {
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
        if (globalValueDao.existsByProject_IdAndNameIgnoreCase(projId, globalValue.getName())) throw new RuntimeException("Global Value name already exists for this project");
        if (globalValueDao.existsByProject_IdAndCodeIgnoreCase(projId, globalValue.getCode())) throw new RuntimeException("Global Value code already exists for this project");

        Projects project = projectDao.findById(projId).orElseThrow(() -> new RuntimeException("Project not found"));
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
        GlobalValue existing = globalValueDao.findById(globalValue.getId()).orElseThrow(() -> new RuntimeException("Global Value not found"));

        if (globalValueDao.existsByProject_IdAndNameIgnoreCaseAndIdNot(projId, globalValue.getName(), globalValue.getId())) throw new RuntimeException("Global Value name already exists for this project");
        if (globalValueDao.existsByProject_IdAndCodeIgnoreCaseAndIdNot(projId, globalValue.getCode(), globalValue.getId())) throw new RuntimeException("Global Value code already exists for this project");

        existing.setName(globalValue.getName());
        existing.setCode(globalValue.getCode());
        existing.setValue(globalValue.getValue());
        globalValueDao.save(existing);
        return "Global Value updated successfully";
    }
}