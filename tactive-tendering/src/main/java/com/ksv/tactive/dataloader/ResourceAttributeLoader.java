package com.ksv.tactive.dataloader;

import com.ksv.tactive.project.dao.*;
import com.ksv.tactive.project.entity.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ResourceAttributeLoader implements MasterDataLoader<ResourceAttributeGroup> {

    @Autowired
    private ResourceDao resourceDao;

    @Autowired
    private AttributeDao attributeDao;

    @Autowired
    private AttributeGroupDao attributeGroupDao;

    @Autowired
    private ResourceAttributeGroupDao resourceAttributeGroupDao;

    @Override
    public void loadData() throws IOException {

        Workbook workbook = new HSSFWorkbook(
                new ClassPathResource("data/Material_Master V1.0.xls").getInputStream()
        );
        Sheet sheet = workbook.getSheetAt(1);

        // ✅ RESOURCE CACHE
        Map<String, Resources> resourceCache = resourceDao.findAll()
                .stream()
                .collect(Collectors.toMap(Resources::getResourceCode, r -> r));

        // ✅ GROUP CACHE
        Map<String, AttributeGroup> groupCache = attributeGroupDao.findAll()
                .stream()
                .collect(Collectors.toMap(
                        g -> g.getGroupName().trim(),
                        g -> g,
                        (e1, e2) -> e1
                ));

        // ✅ ATTRIBUTE CACHE GROUP-WISE (UUID FIX)
        Map<UUID, Map<String, Attribute>> groupAttributeCache = new HashMap<>();

        for (Attribute attr : attributeDao.findAll()) {
            if (attr.getAttributeGroup() == null) continue;

            UUID groupId = attr.getAttributeGroup().getId();

            groupAttributeCache
                    .computeIfAbsent(groupId, k -> new HashMap<>())
                    .put(attr.getAttributeName().trim(), attr);
        }

        // ✅ EXISTING RESOURCE + GROUP
        Map<String, ResourceAttributeGroup> existingMap =
                resourceAttributeGroupDao.findAllWithAttributes()
                        .stream()
                        .collect(Collectors.toMap(
                                r -> r.getResource().getId() + "-" + r.getAttributeGroup().getId(),
                                r -> r,
                                (e1, e2) -> e1
                        ));

        Map<String, ResourceAttributeGroup> saveMap = new HashMap<>();

        List<Attribute> newAttributes = new ArrayList<>();
        List<AttributeGroup> newGroups = new ArrayList<>();

        int batchSize = 500;

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String resourceCode = getCellValue(row.getCell(0));
                String groupName = getCellValue(row.getCell(1));
                String attrString = getCellValue(row.getCell(2));
                String orderStr = getCellValue(row.getCell(3));
                String mandatoryStr = getCellValue(row.getCell(4));

                if (isEmpty(resourceCode) || isEmpty(groupName)) continue;

                // ✅ STEP 1: RESOURCE
                Resources resource = resourceCache.get(resourceCode);
                if (resource == null) continue;

                // ✅ STEP 2: GROUP
                AttributeGroup group = groupCache.get(groupName.trim());

                if (group == null) {
                    group = new AttributeGroup();
                    group.setGroupName(groupName.trim());
                    group.setActive(true);

                    newGroups.add(group);
                    groupCache.put(groupName.trim(), group);
                }

                // ✅ STEP 3: ATTRIBUTES WITHIN GROUP
                Map<String, Attribute> attrMap =
                        groupAttributeCache.computeIfAbsent(group.getId(), k -> new HashMap<>());

                Set<Attribute> finalAttributes = new HashSet<>();

                if (!isEmpty(attrString)) {

                    String[] excelAttrs = attrString.split(",");

                    for (String attr : excelAttrs) {

                        if (isEmpty(attr)) continue;

                        String cleanName = attr.trim();

                        Attribute attribute = attrMap.get(cleanName);

                        // ✅ CREATE IF NOT EXISTS IN THIS GROUP
                        if (attribute == null) {
                            attribute = new Attribute();
                            attribute.setAttributeName(cleanName);
                            attribute.setAttributeGroup(group);
                            attribute.setActive(true);

                            newAttributes.add(attribute);
                            attrMap.put(cleanName, attribute);
                        }

                        finalAttributes.add(attribute);
                    }
                }

                int orderNo = parseInt(orderStr);
                boolean mandatory = isMandatory(mandatoryStr);

                // ✅ STEP 4: MAP RESOURCE + GROUP
                String key = resource.getId() + "-" + group.getId();

                ResourceAttributeGroup rag = existingMap.get(key);

                if (rag == null) {
                    rag = new ResourceAttributeGroup();
                    rag.setResource(resource);
                    rag.setAttributeGroup(group);
                }

                rag.setOrderNo(orderNo);
                rag.setMandatory(mandatory);

                if (rag.getAttributes() == null) {
                    rag.setAttributes(new HashSet<>());
                }

                rag.getAttributes().clear();              // ✅ overwrite safely
                rag.getAttributes().addAll(finalAttributes);

                saveMap.put(key, rag);

                // ✅ BATCH SAVE
                if (newAttributes.size() >= batchSize) {
                    attributeDao.saveAll(newAttributes);
                    newAttributes.clear();
                }

                if (newGroups.size() >= batchSize) {
                    attributeGroupDao.saveAll(newGroups);
                    newGroups.clear();
                }

            } catch (Exception e) {
                System.out.println("Error at row " + i + ": " + e.getMessage());
            }
        }

        // ✅ FINAL SAVE
        if (!newGroups.isEmpty()) {
            attributeGroupDao.saveAll(newGroups);
        }

        if (!newAttributes.isEmpty()) {
            attributeDao.saveAll(newAttributes);
        }

        resourceAttributeGroupDao.saveAll(saveMap.values());

        workbook.close();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> null;
        };
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isMandatory(String value) {
        return value != null &&
                (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("y"));
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int getOrder() {
        return 8;
    }
}