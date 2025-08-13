package com.ksv.tactive.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ksv.tactive.project.dao.UOMDao;
import com.ksv.tactive.project.entity.BOQ;
import com.ksv.tactive.project.entity.UOM;

@Service
public class ExcelFileService {
	
	@Autowired
	private UOMDao uomDao;
	
	private Workbook readXlsxFile(MultipartFile file) throws IOException {
		InputStream inputStream = file.getInputStream();
		Workbook workbook = new XSSFWorkbook(inputStream);
		workbook.close();
		return workbook;
	}
	private Workbook readXlsFile(MultipartFile file) throws IOException {
		InputStream inputStream = file.getInputStream();
		Workbook workbook = new HSSFWorkbook(inputStream);
		workbook.close();
		return workbook;
	}

	public List<String> getSheetNames(MultipartFile file) throws IOException {
		Workbook workbook = null;
		if (file.getOriginalFilename().endsWith(".xlsx")) {
			workbook = readXlsxFile(file);
		} else {
			workbook = readXlsFile(file);
		}
		List<String> sheets = new ArrayList<>();
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			sheets.add(workbook.getSheetName(i));
		}
		return sheets;
	}
	
	
	public List<String> getColumns(MultipartFile boqFile, String sheetName) throws IOException {
		Workbook workbook = null;
		List<String> columns = null;
		if (boqFile.getOriginalFilename().endsWith(".xlsx")) {
			workbook = readXlsxFile(boqFile);
		} else {
			workbook = readXlsFile(boqFile);
		}
		Sheet sheet =workbook.getSheet(sheetName);
		columns = getColumnHeader(sheet);
		return columns;
	}
	
	private List<String> getColumnHeader(Sheet sheet) {
		List<String> columns = new ArrayList<>();
		if(sheet != null) {
			for(int i=0; i<sheet.getPhysicalNumberOfRows(); i++) {
				Row row = sheet.getRow(i); 
				int cell = 0;
				while(row != null && row.getPhysicalNumberOfCells() > cell) {
					if(row.getCell(cell) != null) {
						columns.add(row.getCell(cell).getStringCellValue());
					}
					cell++;
				}
				if(columns.size() >= 4) break;
			}
		}
		return columns;
	}
	public List<BOQ> mapBOQData(String sheetName, Map<String, String> columnMapping, InputStream inputStream) throws EncryptedDocumentException, IOException {
		Sheet sheet = findSheetByName(sheetName, inputStream);
		Map<String, Integer> columnIndex = findColumnIndexMap(sheet, columnMapping);
		List<BOQ> boqList = new ArrayList<>();
		if(sheet != null && !columnIndex.isEmpty()) {
			FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
			int rowNum = findRowNumber(sheet);
			for(int row = rowNum; sheet.getPhysicalNumberOfRows() > row; row++) {
				Row currentRow = sheet.getRow(row);
				if(currentRow != null) {
					BOQ boq = new BOQ();
					for(Map.Entry<String, String> entry : columnMapping.entrySet()) {
						String internalField = entry.getKey();
						String columnHeader = entry.getValue();
						Integer indexValue = columnIndex.get(columnHeader);
						
						if(indexValue != null) {
							Cell cell = currentRow.getCell(indexValue);
							String value = getCellValueAsString(cell, evaluator);
							switch (internalField) {
							case "slNo" -> {
							    Integer parsed = parseInteger(value);
							    if (parsed != null) boq.setSlNo(parsed);
							}
						    case "boqCode" -> boq.setBoqCode(value);
						    case "boqName" -> boq.setBoqName(value);
						    case "parentLevel" -> {
						        if (isNotEmpty(value)) boq.setParentLevel(Integer.valueOf(value));
						    }
						    case "level" -> {
						        if (isNotEmpty(value)) boq.setLevel(Integer.valueOf(value));
						    }
						    case "quantity" -> {
						        if (isNotEmpty(value)) boq.setQuantity(Double.valueOf(value));
						    }
						    case "uom" -> {
						    	if(isNotEmpty(value)) {
						    		UOM uom = uomDao.findByUomCode(value);
						    		boq.setUom(uom);
						    	}
						    }
						    case "supplyRate" -> {
						        if (isNotEmpty(value)) boq.setSupplyRate(Double.valueOf(value));
						    }
						    case "installationRate" -> {
						        if (isNotEmpty(value)) boq.setInstallationRate(Double.valueOf(value));
						    }
						    case "totalRate" -> {
						        if (isNotEmpty(value)) boq.setTotalRate(Double.parseDouble(value));
						    }
						    case "supplyAmount" -> {
						        if (isNotEmpty(value)) boq.setSupplyAmount(Double.parseDouble(value));
						    }
						    case "installationAmount" -> {
						        if (isNotEmpty(value)) boq.setInstallationAmount(Double.parseDouble(value));
						    }
						    case "totalAmount" -> {
						        if (isNotEmpty(value)) boq.setTotalAmount(Double.parseDouble(value));
						    }
						    case "remarks" -> boq.setRemarks(value);
						    case "lastLevel" -> boq.setLastLevel("1".equals(value));
						    case "rateOnly" -> boq.setRateOnly("1".equals(value));
						    case "qtyType" -> boq.setQtyType(value);
						    case "wo" -> {
						        if (isNotEmpty(value)) boq.setWo(Integer.valueOf(value));
						    }
						}
							
						}
					}
					boqList.add(boq);
				}
			}
		}
		return boqList;
	}
	private Integer parseInteger(String value) {
	    if (isNotEmpty(value)) {
	        try {
	            return (int) Double.parseDouble(value); // handles "1", "1.0"
	        } catch (NumberFormatException e) {
	            return null;
	        }
	    }
	    return null;
	}
	private boolean isNotEmpty(String value) {
		return value != null && !value.trim().isEmpty();
	}
	private String getCellValueAsString(Cell cell, FormulaEvaluator evaluator) {
	    if (cell == null) return null;

	    return switch (cell.getCellType()) {
	        case STRING -> cell.getStringCellValue().trim();
	        case NUMERIC -> {
	            double num = cell.getNumericCellValue();
	            if (num == Math.floor(num)) {
	                yield String.valueOf((long) num);
	            } else {
	                yield String.valueOf(num);
	            }
	        }
	        case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
	        case FORMULA -> {
	            CellValue evaluatedValue = evaluator.evaluate(cell);
	            if (evaluatedValue == null) yield null;

	            yield switch (evaluatedValue.getCellType()) {
	                case STRING -> evaluatedValue.getStringValue();
	                case NUMERIC -> {
	                    double num = evaluatedValue.getNumberValue();
	                    if (num == Math.floor(num)) {
	                        yield String.valueOf((long) num);
	                    } else {
	                        yield String.valueOf(num);
	                    }
	                }
	                case BOOLEAN -> String.valueOf(evaluatedValue.getBooleanValue());
	                default -> null;
	            };
	        }
	        default -> null;
	    };
	}
	
	private int findRowNumber(Sheet sheet) {
		List<String> columns = getColumnHeader(sheet);
		for(int row = 0 ; sheet.getPhysicalNumberOfRows() > row; row++) {
			Row currentRow =sheet.getRow(row);
			if(currentRow != null) {
				for(int cell = 0; currentRow.getPhysicalNumberOfCells() > cell; cell++) {
					Cell currentCell = currentRow.getCell(cell);
					if(currentCell != null && currentCell.getCellType() == CellType.STRING) {
						String cellValue = currentCell.getStringCellValue();
						if(cellValue != null && columns.contains(cellValue.trim())) {
							return row+1;
						}
					}
				}
			}	
		}
		return 0;
	}
	private Map<String, Integer> findColumnIndexMap(Sheet sheet, Map<String, String> columnMapping) {
		Map<String, Integer> columnIndexMap = new HashMap<>();
		List<String> columns = getColumnHeader(sheet);
		if(sheet != null) {
			for(int row = 0; sheet.getPhysicalNumberOfRows() > row; row++) {
				Row currentRow = sheet.getRow(row);	
				if(currentRow != null) {
					for(int cell=0; currentRow.getPhysicalNumberOfCells() > cell; cell++) {
						Cell currentCell =currentRow.getCell(cell);
						if(currentCell != null && currentCell.getCellType() == CellType.STRING) {
							String cellValue =currentCell.getStringCellValue();
							if(cellValue != null && columns.contains(cellValue.trim()) && columnMapping.containsValue(cellValue)) {
								columnIndexMap.put(cellValue, cell);
							}
						}
					}
					if(columnIndexMap.size() == columnMapping.size()) {
						break; 
					}
				}
			}
		}
		return columnIndexMap;
	}
	private Sheet findSheetByName(String sheetName, InputStream inputStream) throws EncryptedDocumentException, IOException {
		Workbook workbook = WorkbookFactory.create(inputStream);
		Sheet sheet = workbook !=null ? workbook.getSheet(sheetName) : null;
		return sheet != null ? sheet : null;
	}

}
