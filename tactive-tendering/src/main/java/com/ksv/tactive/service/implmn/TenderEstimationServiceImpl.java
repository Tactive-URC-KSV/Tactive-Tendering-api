package com.ksv.tactive.service.implmn;

import com.ksv.tactive.dto.*;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.exception.DuplicateEntryException;
import com.ksv.tactive.project.dao.*;
import com.ksv.tactive.project.entity.*;
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
        if (value == null || value.trim().isEmpty()) return null;
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

        if (dto.getInternalBoqId() != null && !dto.getInternalBoqId().isEmpty()) {
            InternalBoq targetIb = internalBoqDao.findById(convertToUUID(dto.getInternalBoqId()))
                    .orElseThrow(() -> new RuntimeException("Internal BOQ not found"));

            // Use a copy to prevent ConcurrentModificationException during loop iteration
            List<TenderEstimation> owners = new ArrayList<>(targetIb.getParentResource());
            for (TenderEstimation owner : owners) {
                processSingleEstimationEntry(owner, targetIb, null, dto);
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

            BOQ boq = boqDao.findById(convertToUUID(targetBoqId))
                    .orElseThrow(() -> new RuntimeException("BOQ not found"));

            processSingleEstimationEntry(null, null, boq, dto);
        }

        return "Saved Successfully";
    }

    private void processSingleEstimationEntry(TenderEstimation parentTE, InternalBoq containerIB, BOQ boq, TenderResourceDto dto) {
        UUID resourceId = convertToUUID(dto.getResourceId());
        UUID uomId = convertToUUID(dto.getUomId());
        List<UUID> attributeIds = dto.getAttributes() != null
                ? dto.getAttributes().stream().map(a -> convertToUUID(a.getAttributeId())).toList()
                : new ArrayList<>();

        Optional<TenderEstimation> duplicateOpt = findDuplicateForSpecificParent(containerIB, boq, parentTE, resourceId, uomId, attributeIds, dto.getResourceNatureId());

        TenderEstimation tenderEst;
        if (duplicateOpt.isPresent()) {
            tenderEst = duplicateOpt.get();
            if (ResourceNature.SIMPLE.name().equalsIgnoreCase(dto.getResourceNatureId())) {
                throw new DuplicateEntryException("This simple resource already exists for this entry.");
            }
        } else {
            tenderEst = new TenderEstimation();
            tenderEst.setProject(projectDao.findById(convertToUUID(dto.getProjectId())).orElse(null));
            tenderEst.setParentInternalBoq(containerIB);
            tenderEst.setParent(parentTE);
            tenderEst.setBoq(boq);

            updateTenderEstimationFields(tenderEst, dto);

            int maxRefCode = 0;
            if (parentTE != null) {
                maxRefCode = tenderEstDao.findByParent_Id(parentTE.getId()).stream()
                        .mapToInt(TenderEstimation::getRefCode).max().orElse(0);
            } else if (boq != null) {
                maxRefCode = tenderEstDao.findByBoqId(boq.getId()).stream()
                        .filter(e -> e.getParent() == null)
                        .mapToInt(TenderEstimation::getRefCode).max().orElse(0);
            }
            tenderEst.setRefCode(maxRefCode + 1);
        }

        saveFormulaElements(tenderEst, dto);
        calculateValues(tenderEst, dto);

        // saveAndFlush prevents TransientObjectException when downstream calculations query the DB
        tenderEst = tenderEstDao.saveAndFlush(tenderEst);
        propagateCost(tenderEst);
    }

    private Optional<TenderEstimation> findDuplicateForSpecificParent(InternalBoq containerIB, BOQ boq, TenderEstimation parent, UUID resId, UUID uomId, List<UUID> attrs, String natureId) {
        List<TenderEstimation> candidates;

        if (ResourceNature.SIMPLE.name().equalsIgnoreCase(natureId) && boq != null) {
            candidates = tenderEstDao.findByBoq_IdAndProject_IdAndResources_Id(boq.getId(), convertToUUID(boq.getProjectId()), resId);
        } else if (containerIB != null) {
            candidates = tenderEstDao.findByParentInternalBoq_IdAndResources_IdAndUom_Id(containerIB.getId(), resId, uomId);
            if (parent != null) {
                candidates = candidates.stream().filter(c -> c.getParent() != null && c.getParent().getId().equals(parent.getId())).collect(Collectors.toList());
            }
        } else if (boq != null) {
            candidates = tenderEstDao.findByBoq_IdAndParentIsNullAndResources_IdAndUom_Id(boq.getId(), resId, uomId);
        } else {
            candidates = new ArrayList<>();
        }

        for (TenderEstimation est : candidates) {
            if (isSameTenderAttributes(est, attrs)) return Optional.of(est);
        }
        return Optional.empty();
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

        tenderEst = tenderEstDao.saveAndFlush(tenderEst);
        propagateCost(tenderEst);

        return "Updated Successfully";
    }

    private void propagateCost(TenderEstimation node) {
        if (node.getResourceNature() == ResourceNature.COMPLEX) {
            InternalBoq iboq = findOrCreateInternalBoq(node);
            syncInternalBoqAndParents(iboq);
        }

        if (node.getParentInternalBoq() != null) {
            syncInternalBoqAndParents(node.getParentInternalBoq());
        } else if (node.getBoq() != null) {
            updateBoq(node.getBoq());
            updateProject(node.getBoq().getProjectId().toString());
        }
    }

    private void syncInternalBoqAndParents(InternalBoq iboq) {
        List<TenderEstimation> children = tenderEstDao.findByParentInternalBoq_Id(iboq.getId());

        double totalChildAmount = children.stream().mapToDouble(TenderEstimation::getTotalCostCompanyCurrency).sum();
        double totalOwnerQty = iboq.getParentResource().stream().mapToDouble(TenderEstimation::getNetQuantity).sum();

        iboq.setTotalQuantity(totalOwnerQty);
        iboq.setAmount(totalChildAmount);

        if (totalOwnerQty > 0) {
            iboq.setRate(totalChildAmount / totalOwnerQty);
        } else {
            iboq.setRate(0.0);
        }

        internalBoqDao.saveAndFlush(iboq);

        // Copy collection to avoid ConcurrentModificationException during updates
        List<TenderEstimation> parentResources = new ArrayList<>(iboq.getParentResource());
        for (TenderEstimation parent : parentResources) {
            parent.setCostUnitRate(iboq.getRate());
            parent.setTotalCostCompanyCurrency(parent.getNetQuantity() * iboq.getRate());
            parent.setResourceTotalCost(parent.getTotalCostCompanyCurrency() * parent.getExchangeRate());
            tenderEstDao.saveAndFlush(parent);

            if (parent.getParentInternalBoq() != null) {
                syncInternalBoqAndParents(parent.getParentInternalBoq());
            } else if (parent.getBoq() != null) {
                updateBoq(parent.getBoq());
                updateProject(parent.getBoq().getProjectId().toString());
            }
        }
    }

    private void calculateValues(TenderEstimation t, TenderResourceDto dto) {
        double unitMultiplier = (t.getQuantityType() == QuantityType.FORMULA) ? evaluateFormula(t.getFormulaElements()) : t.getCoEfficient();
        double calculatedQty;

        if (t.getParentInternalBoq() != null && t.getParent() != null) {
            calculatedQty = t.getParent().getNetQuantity() * unitMultiplier;
        } else {
            double boqQty = (t.getBoq() != null) ? t.getBoq().getQuantity() : 0.0;
            if (t.getQuantityType() == QuantityType.DIRECT) {
                calculatedQty = boqQty;
            } else if (t.getQuantityType() == QuantityType.FORMULA) {
                calculatedQty = unitMultiplier;
            } else {
                calculatedQty = boqQty * unitMultiplier;
            }
        }

        double wasteQty = (dto.getWastePercentage() / 100) * calculatedQty;
        t.setCalculatedQuantity(calculatedQty);
        t.setWasteQuantity(wasteQty);
        t.setNetQuantity(calculatedQty + wasteQty);
        t.setTotalCostCompanyCurrency(t.getNetQuantity() * t.getCostUnitRate());
        t.setResourceTotalCost(t.getTotalCostCompanyCurrency() * t.getExchangeRate());
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

    private void updateProject(String projectId) {
        Projects project = projectDao.findById(convertToUUID(projectId)).orElse(null);
        List<BOQ> boqList = boqDao.findByProjectId(projectId);
        double total = boqList.stream().mapToDouble(BOQ::getTotalAmount).sum();
        if (project != null) {
            project.setTotalEstimatedValue(BigDecimal.valueOf(total));
            projectDao.save(project);
        }

    }

    private InternalBoq findOrCreateInternalBoq(TenderEstimation est) {
        List<InternalBoq> existingList = internalBoqDao.findByProject_IdAndResource_IdAndUom_Id(est.getProject().getId(), est.getResources().getId(), est.getUom().getId());
        for (InternalBoq boq : existingList) {
            if (isSameInternalAttributes(boq, est.getEstimationAttributes())) {
                if (!boq.getParentResource().contains(est)) {
                    boq.getParentResource().add(est);
                    internalBoqDao.saveAndFlush(boq);
                }
                return boq;
            }
        }
        InternalBoq newBoq = new InternalBoq();
        newBoq.setProject(est.getProject());
        newBoq.setResource(est.getResources());
        newBoq.setUom(est.getUom());
        newBoq.setAttributeGroup(resolveAttributeGroup(est));
        newBoq.setAttributes(mapAttributes(est, newBoq));
        newBoq.getParentResource().add(est);
        return internalBoqDao.saveAndFlush(newBoq);
    }

    private AttributeGroup resolveAttributeGroup(TenderEstimation est) {
        if (est.getEstimationAttributes() == null || est.getEstimationAttributes().isEmpty()) return null;
        return est.getEstimationAttributes().stream()
                .map(attr -> attr.getAttribute().getAttributeGroup())
                .filter(Objects::nonNull).findFirst().orElse(null);
    }

    private boolean isSameTenderAttributes(TenderEstimation est, List<UUID> attributeIds) {
        if (est.getEstimationAttributes().size() != attributeIds.size()) return false;
        Set<UUID> existingIds = est.getEstimationAttributes().stream().map(a -> a.getAttribute().getId()).collect(Collectors.toSet());
        return existingIds.equals(new HashSet<>(attributeIds));
    }

    private boolean isSameInternalAttributes(InternalBoq boq, List<TenderEstimationAttribute> attrs) {
        if (boq.getAttributes().size() != attrs.size()) return false;
        Set<UUID> boqAttrIds = boq.getAttributes().stream().map(a -> a.getAttribute().getId()).collect(Collectors.toSet());
        Set<UUID> incomingAttrIds = attrs.stream().map(a -> a.getAttribute().getId()).collect(Collectors.toSet());
        return boqAttrIds.equals(incomingAttrIds);
    }

    private List<InternalBoqAttribute> mapAttributes(TenderEstimation est, InternalBoq newBoq) {
        return est.getEstimationAttributes().stream().map(attr -> {
            InternalBoqAttribute i = new InternalBoqAttribute();
            i.setAttribute(attr.getAttribute());
            i.setInternalBoq(newBoq);
            return i;
        }).collect(Collectors.toList());
    }

    private double evaluateFormula(List<TenderFormulaElement> elements) {
        if (elements == null || elements.isEmpty()) return 0;

        // Prevent ConcurrentModificationException when sorting a persistent bag
        List<TenderFormulaElement> sortedElements = new ArrayList<>(elements);
        sortedElements.sort(Comparator.comparingInt(TenderFormulaElement::getSequence));

        List<Double> values = new ArrayList<>();
        List<String> operators = new ArrayList<>();
        for (TenderFormulaElement el : sortedElements) {
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
        if (values.isEmpty()) return 0;
        double result = values.get(0);
        for (int i = 0; i < operators.size(); i++) {
            String op = operators.get(i);
            double next = (i + 1 < values.size()) ? values.get(i + 1) : 0;
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

            if (f.getSourceType() != null) {
                el.setSourceType(FormulaSourceType.valueOf(f.getSourceType()));
            }

            if ("BOQ".equalsIgnoreCase(f.getSourceType())) {
                el.setBoq(boqDao.findById(convertToUUID(f.getRefId())).orElseThrow(() -> new RuntimeException("BOQ not found")));
            } else if ("RESOURCE".equalsIgnoreCase(f.getSourceType())) {
                el.setResource(tenderEstDao.findById(convertToUUID(f.getRefId())).orElseThrow(() -> new RuntimeException("Resource not found")));
            } else if ("GLOBAL".equalsIgnoreCase(f.getSourceType())) {
                el.setGlobalValue(globalValueDao.findById(convertToUUID(f.getRefId())).orElseThrow(() -> new RuntimeException("Global value not found")));
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
        tenderEst.setDocNumber(dto.getDocNumber());
        tenderEst.setCoEfficient(dto.getCoEfficient());
        tenderEst.setWastePercentage(dto.getWastePercentage());
        tenderEst.setRate(dto.getRate());
        tenderEst.setAdditionalRate(dto.getAdditionalRate());
        tenderEst.setShippingPrice(dto.getShippingPrice());
        tenderEst.setCostUnitRate(dto.getCostUnitRate());
        tenderEst.setRateLock(dto.isRateLock());
        tenderEst.setExchangeRate(dto.getExchangeRate());

        tenderEst.setResourceType(dto.getResourceTypeId() != null && !dto.getResourceTypeId().isEmpty() ? ResourceType.valueOf(dto.getResourceTypeId()) : null);
        tenderEst.setResources(dto.getResourceId() != null && !dto.getResourceId().trim().isEmpty() ? masterService.findResourceById(dto.getResourceId()) : null);
        tenderEst.setUom(dto.getUomId() != null && !dto.getUomId().trim().isEmpty() ? masterService.findUomById(dto.getUomId()) : null);
        tenderEst.setResourceNature(dto.getResourceNatureId() != null && !dto.getResourceNatureId().isEmpty() ? ResourceNature.valueOf(dto.getResourceNatureId().toUpperCase()) : null);
        tenderEst.setQuantityType(dto.getQuantityTypeId() != null && !dto.getQuantityTypeId().trim().isEmpty() ? QuantityType.valueOf(dto.getQuantityTypeId().toUpperCase()) : null);
        tenderEst.setCurrency(dto.getCurrencyId() != null && !dto.getCurrencyId().trim().isEmpty() ? masterService.findCurrencyById(dto.getCurrencyId()) : null);

        if (dto.getAttributes() != null) {
            tenderEst.getEstimationAttributes().clear();
            for (AttributeSelectionDto d : dto.getAttributes()) {
                TenderEstimationAttribute attribute = new TenderEstimationAttribute();
                attribute.setTenderEstimation(tenderEst);
                attribute.setAttributeGroup(attributeGroupDao.findById(convertToUUID(d.getAttributeGroupId())).orElse(null));
                attribute.setAttribute(attributeDao.findById(convertToUUID(d.getAttributeId())).orElse(null));
                tenderEst.getEstimationAttributes().add(attribute);
            }
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
        TenderEstimation parent = tenderEstimation.getParent();

        List<TenderEstimation> children = tenderEstDao.findByParent_Id(tenderEstimation.getId());
        for (TenderEstimation child : children) {
            deleteTenderEstimationForBoq(child.getId().toString(), boqId);
        }

        Set<InternalBoq> relatedBoqs = new HashSet<>();
        if (tenderEstimation.getParentInternalBoq() != null) {
            relatedBoqs.add(tenderEstimation.getParentInternalBoq());
        }
        for (InternalBoq ib : relatedBoqs) {
            cleanupInternalBoq(ib, tenderEstimation.getId());
        }

        tenderEstDao.delete(tenderEstimation);

        if (parent != null) {
            propagateCost(parent);
        } else if (boq != null) {
            updateBoq(boq);
            updateProject(boq.getProjectId().toString());
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

            double unitRate = children.stream().filter(c -> !c.getId().equals(currentEstId))
                    .mapToDouble(TenderEstimation::getTotalCostCompanyCurrency).sum();

            iBoq.setTotalQuantity(totalQty);
            iBoq.setRate(totalQty > 0 ? unitRate / totalQty : 0.0);
            iBoq.setAmount(totalQty * iBoq.getRate());
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