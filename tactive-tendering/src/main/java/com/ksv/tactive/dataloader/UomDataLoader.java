package com.ksv.tactive.dataloader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ksv.tactive.project.dao.UomConversionDao;
import com.ksv.tactive.project.entity.Uom;
import com.ksv.tactive.project.entity.UomConversion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksv.tactive.project.dao.UomDao;

@Component
public class UomDataLoader implements MasterDataLoader<Uom> {

    @Autowired
    private UomDao uomDao;

    @Autowired
    private UomConversionDao conversionDao;

    @Override
    public void loadData() throws IOException {

        ClassPathResource resource = new ClassPathResource("data/UOM.xlsx");
        Workbook workbook = new XSSFWorkbook(resource.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Map<String, Uom> uomMap = new HashMap<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            String code = getCellValue(row.getCell(0));
            String name = getCellValue(row.getCell(1));
            String category = getCellValue(row.getCell(2));
            if (code == null) continue;
            Uom uom = saveOrGetUom(code, name, category);
            uomMap.put(code, uom);
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            String baseCode = getCellValue(row.getCell(0));
            String conversionCode = getCellValue(row.getCell(3));
            String formula = getCellValue(row.getCell(5));
            if (baseCode == null || conversionCode == null || formula == null) continue;
            Uom baseUom = uomMap.get(baseCode);
            Uom conversionUom = uomMap.get(conversionCode);

            if (baseUom != null && conversionUom != null) {
                saveConversion(baseUom, conversionUom, formula);
            }
        }
    }
    private Uom saveOrGetUom(String code, String name, String category) {
        Uom existing = uomDao.findByUomCode(code);
        if (existing != null) {
            return existing;
        }
        Uom uom = new Uom();
        uom.setUomCode(code);
        uom.setUomName(name);
        uom.setActive(true);
        return uomDao.save(uom);
    }
    private void saveConversion(Uom fromUom, Uom toUom, String formula) {
        boolean exists = conversionDao
                .existsByBaseUomAndConversionUom(fromUom,toUom);
        if (exists) return;
        UomConversion conversion = new UomConversion();
        conversion.setBaseUom(fromUom);
        conversion.setConversionUom(toUom);
        conversion.setFormula(formula);
        conversionDao.save(conversion);
    }
    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> null;
        };
    }

    @Override
    public int getOrder() {
        return 5;
    }
}