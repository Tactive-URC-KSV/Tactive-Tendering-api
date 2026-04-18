package com.ksv.tactive.dataloader;

import com.ksv.tactive.project.dao.ResourceAttributeGroupDao;
import com.ksv.tactive.project.dao.ResourceDao;
import com.ksv.tactive.project.dao.UomDao;
import com.ksv.tactive.project.entity.Resources;
import com.ksv.tactive.project.entity.Uom;
import com.ksv.tactive.util.ResourceType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ResourceMaterialMasterLoader implements MasterDataLoader<Resources> {
    @Autowired
    private ResourceDao resourceDao;
    @Autowired
    private UomDao uomDao;
    @Override
    public void loadData() throws IOException, DataAccessException {
        loadMaterialMaster();
        loadMachinaryMaster();

    }

    private void loadMachinaryMaster() throws IOException {

        ClassPathResource resource = new ClassPathResource("data/Machinery_Formwork.xls");
        Workbook workbook = new HSSFWorkbook(resource.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Map<String, Uom> uomCache = uomDao.findAll()
                .stream()
                .collect(Collectors.toMap(Uom::getUomCode, u -> u));
        Map<String, Resources> resourceCache = resourceDao.findAll()
                .stream()
                .collect(Collectors.toMap(Resources::getResourceCode, r -> r));
        Map<Integer, Map<String, Resources>> levelMap = new HashMap<>();
        List<Resources> batch = new ArrayList<>();
        int batchSize = 500;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String code = getCellValue(row.getCell(0));
                String name = getCellValue(row.getCell(1));
                String parentName = getCellValue(row.getCell(2));
                String levelStr = getCellValue(row.getCell(3));
                String lastLevelStr = getCellValue(row.getCell(4));
                String lastParentStr = getCellValue(row.getCell(5));
                String uomCode = getCellValue(row.getCell(6));
                if (isEmpty(code) || isEmpty(name)) continue;
                int level = parseInt(levelStr);
                boolean lastLevel = parseBoolean(lastLevelStr);
                boolean lastParent = parseBoolean(lastParentStr);
                Uom uom = uomCache.get(uomCode);
                Resources entity = resourceCache.get(code);
                if (entity == null) {
                    entity = new Resources();
                    entity.setResourceCode(code);
                    resourceCache.put(code, entity);
                }
                entity.setResourceName(name);
                entity.setLevel(level);
                entity.setLastLevel(lastLevel);
                entity.setResourceType(ResourceType.MACHINERY);
                entity.setLastParent(lastParent);
                entity.setUom(uom);
                entity.setActive(true);
                if (level == 1) {
                    entity.setParent(null);
                } else {
                    Map<String, Resources> parentLevelMap = levelMap.get(level - 1);
                    if (parentLevelMap != null) {
                        Resources parent = parentLevelMap.get(parentName);
                        entity.setParent(parent);
                    } else {
                        entity.setParent(null);
                    }
                }
                levelMap
                        .computeIfAbsent(level, k -> new HashMap<>())
                        .put(name, entity);
                batch.add(entity);
                if (batch.size() >= batchSize) {
                    resourceDao.saveAll(batch);
                    batch.clear();
                }
            } catch (Exception e) {
                System.out.println("Error at row " + i + ": " + e.getMessage());
            }
        }
        if (!batch.isEmpty()) {
            resourceDao.saveAll(batch);
        }
        workbook.close();
    }
    private void loadMaterialMaster() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/Material_Master V1.0.xls");
        Workbook workbook = new HSSFWorkbook(resource.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Map<String, Uom> uomCache = uomDao.findAll()
                .stream()
                .collect(Collectors.toMap(Uom::getUomCode, u -> u));
        Map<String, Resources> resourceCache = resourceDao.findAll()
                .stream()
                .collect(Collectors.toMap(Resources::getResourceCode, r -> r));
        Map<Integer, Map<String, Resources>> levelMap = new HashMap<>();
        List<Resources> batch = new ArrayList<>();
        int batchSize = 500;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            try {
                String code = getCellValue(row.getCell(0));
                String name = getCellValue(row.getCell(1));
                String parentName = getCellValue(row.getCell(2));
                String levelStr = getCellValue(row.getCell(3));
                String lastLevelStr = getCellValue(row.getCell(4));
                String uomCode = getCellValue(row.getCell(5));
                if (isEmpty(code) || isEmpty(name)) continue;
                int level = parseInt(levelStr);
                boolean lastLevel = parseBoolean(lastLevelStr);
                Uom uom = uomCache.get(uomCode);
                Resources entity = resourceCache.get(code);
                if (entity == null) {
                    entity = new Resources();
                    entity.setResourceCode(code);
                    resourceCache.put(code, entity);
                }
                entity.setResourceName(name);
                entity.setLevel(level);
                entity.setLastLevel(lastLevel);
                entity.setResourceType(ResourceType.MATERIALS);
                entity.setUom(uom);
                entity.setActive(true);
                if (level == 1) {
                    entity.setParent(null);
                } else {
                    Map<String, Resources> parentLevelMap = levelMap.get(level - 1);
                    if (parentLevelMap != null) {
                        Resources parent = parentLevelMap.get(parentName);
                        entity.setParent(parent);
                    } else {
                        entity.setParent(null);
                    }
                }
                levelMap
                        .computeIfAbsent(level, k -> new HashMap<>())
                        .put(name, entity);
                batch.add(entity);
                if (batch.size() >= batchSize) {
                    resourceDao.saveAll(batch);
                    batch.clear();
                }
            } catch (Exception e) {
                System.out.println("Error processing row " + i + ": " + e.getMessage());
            }
        }
        if (!batch.isEmpty()) {
            resourceDao.saveAll(batch);
        }
        workbook.close();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                if (val == (long) val) {
                    yield String.valueOf((long) val);
                } else {
                    yield String.valueOf(val);
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }
    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
    private int parseInt(String value) {
        try {
            return (int) Double.parseDouble(value);
        } catch (Exception e) {
            return 0;
        }
    }
    private boolean parseBoolean(String value) {
        if (value == null) return false;
        value = value.trim().toLowerCase();
        return value.equals("true")
                || value.equals("1")
                || value.equals("yes")
                || value.equals("y");
    }

    @Override
    public int getOrder() {
        return 7;
    }
}
