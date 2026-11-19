package com.ksv.tactive.dataloader;

import com.ksv.tactive.project.dao.AttributeDao;
import com.ksv.tactive.project.dao.AttributeGroupDao;
import com.ksv.tactive.project.entity.Attribute;
import com.ksv.tactive.project.entity.AttributeGroup;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AttributeLoader implements MasterDataLoader<Attribute> {

    @Autowired
    private AttributeDao attributeDao;

    @Autowired
    private AttributeGroupDao attributeGroupDao;

    @Override
    @Transactional
    public void loadData() throws IOException, DataAccessException {

        ClassPathResource resource = new ClassPathResource("data/Attributes.xlsx");
        Workbook workbook = new XSSFWorkbook(resource.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        // ✅ LOAD ONCE
        List<AttributeGroup> groupList = attributeGroupDao.findAll();
        List<Attribute> attributeList = attributeDao.findAll();

        // ✅ SAFE GROUP CACHE
        Map<Long, AttributeGroup> groupCache = groupList.stream()
                .collect(Collectors.toMap(
                        g -> g.getGroupCode(),
                        g -> g,
                        (existing, duplicate) -> existing   // ✅ FIX
                ));

        Map<String, AttributeGroup> groupNameCache = groupList.stream()
                .collect(Collectors.toMap(
                        g -> normalize(g.getGroupName()),
                        g -> g,
                        (existing, duplicate) -> existing
                ));

        // ✅ SAFE ATTRIBUTE CACHE
        Map<Long, Attribute> attributeCache = attributeList.stream()
                .collect(Collectors.toMap(
                        a -> a.getAttributeCode(),
                        a -> a,
                        (existing, duplicate) -> existing
                ));

        Map<String, Attribute> attributeNameCache = attributeList.stream()
                .collect(Collectors.toMap(
                        a -> normalize(a.getAttributeName()),
                        a -> a,
                        (existing, duplicate) -> existing
                ));

        Set<String> processedMappings = new HashSet<>();
        List<AttributeGroup> newGroups = new ArrayList<>();
        List<Attribute> batch = new ArrayList<>();

        int batchSize = 500;

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String groupCodeStr = getCellValue(row.getCell(0));
                String groupName = getCellValue(row.getCell(1));
                String attributeCodeStr = getCellValue(row.getCell(2));
                String attributeName = getCellValue(row.getCell(3));

                if (isEmpty(groupCodeStr) || isEmpty(groupName)
                        || isEmpty(attributeCodeStr) || isEmpty(attributeName)) {
                    continue;
                }

                long groupCode = parseLong(groupCodeStr);
                long attributeCode = parseLong(attributeCodeStr);

                if (groupCode == -1 || attributeCode == -1) continue;

                String normalizedGroupName = normalize(groupName);
                String normalizedAttrName = normalize(attributeName);

                String mappingKey = normalizedAttrName + "-" + groupCode;
                if (!processedMappings.add(mappingKey)) continue;

                // ✅ GROUP RESOLUTION
                AttributeGroup group = groupCache.get(groupCode);

                if (group == null) {
                    group = groupNameCache.get(normalizedGroupName);
                }

                if (group == null) {
                    group = new AttributeGroup();
                    group.setGroupCode(groupCode);
                    group.setGroupName(groupName.trim());
                    group.setActive(true);

                    groupCache.put(groupCode, group);
                    groupNameCache.put(normalizedGroupName, group);
                    newGroups.add(group);
                }

                // ✅ ATTRIBUTE RESOLUTION
                Attribute attribute = attributeCache.get(attributeCode);

                if (attribute == null) {
                    attribute = attributeNameCache.get(normalizedAttrName);
                }

                if (attribute == null) {
                    attribute = new Attribute();
                    attribute.setAttributeCode(attributeCode);
                    attribute.setAttributeName(attributeName.trim());
                    attribute.setActive(true);

                    attributeCache.put(attributeCode, attribute);
                    attributeNameCache.put(normalizedAttrName, attribute);
                }

                // ✅ UPDATE ONLY IF NEEDED
                if (attribute.getAttributeGroup() == null ||
                        !Objects.equals(attribute.getAttributeGroup().getGroupCode(), groupCode)) {

                    attribute.setAttributeGroup(group);
                    batch.add(attribute);   // ✅ only when changed
                }

                // ✅ BATCH SAVE
                if (batch.size() >= batchSize) {

                    if (!newGroups.isEmpty()) {
                        attributeGroupDao.saveAll(newGroups);
                        newGroups.clear();
                    }

                    attributeDao.saveAll(batch);
                    batch.clear();
                }

            } catch (Exception e) {
                System.out.println("Error at row " + i + ": " + e.getMessage());
            }
        }

        // ✅ FINAL SAVE
        if (!newGroups.isEmpty()) {
            attributeGroupDao.saveAll(newGroups);
        }

        if (!batch.isEmpty()) {
            attributeDao.saveAll(batch);
        }

        workbook.close();
    }

    private String normalize(String value) {
        if (value == null) return null;
        return value.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", "")
                .replaceAll("\\s+", " ");
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

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public int getOrder() {
        return 6;
    }
}