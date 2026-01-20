package com.ksv.tactive.fileservice;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import com.ksv.tactive.dto.BOQDto;
import com.ksv.tactive.dto.BOQSummaryDTO;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ksv.tactive.dto.ExcelExtractBOQ;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.project.entity.UOM;
import com.ksv.tactive.project.entity.BOQ;
import com.ksv.tactive.service.MasterService;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class ExcelFileService {
	
	@Autowired
	private MasterService masterService;

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
		Sheet sheet = workbook.getSheet(sheetName);
		columns = getColumnHeader(sheet);
		return columns;
	}

	private List<String> getColumnHeader(Sheet sheet) {
		List<String> columns = new ArrayList<>();
		if (sheet != null) {
			for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
				Row row = sheet.getRow(i);
				int cell = 0;
				while (row != null && row.getPhysicalNumberOfCells() > cell) {
					if (row.getCell(cell) != null) {
						columns.add(row.getCell(cell).getStringCellValue());
					}
					cell++;
				}
				if (columns.size() >= 4)
					break;
			}
		}
		return columns.stream()
				.filter(val -> val != null && !val.trim().isEmpty())
				.collect(Collectors.toList());
	}

	public List<BOQ> mapBOQData(String sheetName, Map<String, String> columnMapping, InputStream inputStream)
			throws EncryptedDocumentException, IOException, DataNotFoundException {
		Sheet sheet = findSheetByName(sheetName, inputStream);
		Map<String, Integer> columnIndex = findColumnIndexMap(sheet, columnMapping);
		List<BOQ> boqList = new ArrayList<>();
		if (sheet != null && !columnIndex.isEmpty()) {
			FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
			int rowNum = findRowNumber(sheet);
			for (int row = rowNum; sheet.getPhysicalNumberOfRows() > row; row++) {
				Row currentRow = sheet.getRow(row);
				if (currentRow != null) {
					BOQ boq = new BOQ();
					for (Map.Entry<String, String> entry : columnMapping.entrySet()) {
						String internalField = entry.getKey();
						String columnHeader = entry.getValue();
						Integer indexValue = columnIndex.get(columnHeader);
						if (indexValue != null) {
							Cell cell = currentRow.getCell(indexValue);
							String value = getCellValueAsString(cell, evaluator);
							switch (internalField) {
								case "boqCode" -> boq.setBoqCode(value);
								case "boqName" -> boq.setBoqName(value);
								case "quantity" -> {
									if (isNotEmpty(value)) {
										boq.setQuantity(Double.valueOf(value));
										boq.setLastLevel(true);
									}
								}
								case "uom" -> {
									if (isNotEmpty(value)) {
//										boq.setUom(findUOM(value));
										boq.setLastLevel(true);
									}
									else {
										boq.setLastLevel(false);
									}
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


	private boolean isNotEmpty(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String getCellValueAsString(Cell cell, FormulaEvaluator evaluator) {
		if (cell == null)
			return null;
		return switch (cell.getCellType()) {
		case STRING ->{
			String value = cell.getStringCellValue().trim();
			if(value.isEmpty()) {
				yield null;
			} else {
				yield value.toLowerCase().contains("total amount") ? null : value;
			}
		}
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
			if (evaluatedValue == null)
				yield null;
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
		for (int row = 0; sheet.getPhysicalNumberOfRows() > row; row++) {
			Row currentRow = sheet.getRow(row);
			if (currentRow != null) {
				for (int cell = 0; currentRow.getPhysicalNumberOfCells() > cell; cell++) {
					Cell currentCell = currentRow.getCell(cell);
					if (currentCell != null && currentCell.getCellType() == CellType.STRING) {
						String cellValue = currentCell.getStringCellValue();
						if (cellValue != null && columns.contains(cellValue.trim())) {
							return row + 1;
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
		if (sheet != null) {
			for (int row = 0; sheet.getPhysicalNumberOfRows() > row; row++) {
				Row currentRow = sheet.getRow(row);
				if (currentRow != null) {
					for (int cell = 0; currentRow.getPhysicalNumberOfCells() > cell; cell++) {
						Cell currentCell = currentRow.getCell(cell);
						if (currentCell != null && currentCell.getCellType() == CellType.STRING) {
							String cellValue = currentCell.getStringCellValue();
							if (cellValue != null && columns.contains(cellValue.trim())
									&& columnMapping.containsValue(cellValue)) {
								columnIndexMap.put(cellValue, cell);
							}
						}
					}
					if (columnIndexMap.size() == columnMapping.size()) {
						break;
					}
				}
			}
		}
		return columnIndexMap;
	}

	private Sheet findSheetByName(String sheetName, InputStream inputStream)
			throws EncryptedDocumentException, IOException {
		Workbook workbook = WorkbookFactory.create(inputStream);
		Sheet sheet = workbook != null ? workbook.getSheet(sheetName) : null;
		return sheet != null ? sheet : null;
	}


	public void generateBoqFile(List<BOQ> boqList, HttpServletResponse response) throws IOException {
		String projectName = boqList.get(0).getProject().getProjectName().trim().replaceAll("\\s+", "_");
		String fileName = projectName;
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");
		XSSFWorkbook workBook = new XSSFWorkbook();
		XSSFSheet sheet = workBook.createSheet("BOQ");
		addData(sheet, boqList);
		workBook.write(response.getOutputStream());
		response.getOutputStream().flush();
		response.getOutputStream().close();
		workBook.close();
	}

	private void addData(XSSFSheet sheet, List<BOQ> boqList) {
		sheet.setColumnWidth(2, 256 * 10);
		sheet.setColumnWidth(3, 256 * 50);
		sheet.setColumnWidth(7, 256 * 15);
		sheet.setColumnWidth(8, 256 * 15);
		addHeader(sheet);
		int rowNum = 1;
		for (BOQ boq : boqList) {
			int slNo = rowNum++;
			Row row = sheet.createRow(slNo);
			row.createCell(0).setCellValue(slNo);
			row.createCell(1).setCellValue(boq.getId());
			row.createCell(2).setCellValue(boq.getBoqCode());
			row.createCell(3).setCellValue(boq.getBoqName());
			row.createCell(4).setCellValue(boq.getUom() != null ? getUOMCode(boq.getUom().getId()) : "");
			row.createCell(5).setCellValue(boq.getQuantity());
			row.createCell(8).setCellValue(boq.getParentBOQ() != null ? boq.getParentBOQ().getBoqCode() : "");
			row.createCell(9).setCellValue(boq.isLastLevel() ? "Yes" : "No");
		}
	}

	private String getUOMCode(String uomId) {
		UOM uom = masterService.findUomById(uomId);
		if (uom != null) {
			return uom.getUomCode();
		}
		return null;
	}

	private void addHeader(XSSFSheet sheet) {
		Row row = sheet.createRow(0);
		CellStyle style = sheet.getWorkbook().createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setWrapText(true);
		row.setRowStyle(style);
		String[] header = { "S No", "ID", "BOQ Code", "BOQ Name", "UOM", "Quantity", "Rate", "Amount", "Parent Code",
				"Last level" };
		for (int i = 0; i < header.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(header[i]);
		}
	}

	public List<ExcelExtractBOQ> getExtractedData(
	        MultipartFile boqFile, String sheetName, Map<String, String> columnMapping,
	        int startRow, int endRow) throws IOException {
	    Workbook workbook = boqFile.getOriginalFilename().endsWith(".xlsx")
	            ? readXlsxFile(boqFile) : readXlsFile(boqFile);
	    Sheet sheet = workbook.getSheet(sheetName);
	    if (sheet == null) {
	        return new ArrayList<>();
	    }
	    Map<String, Integer> columnIndex = findColumnIndexMap(sheet, columnMapping);
	    if (columnIndex.isEmpty()) {
	    }
	    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
	    int lastRowNum = sheet.getLastRowNum();
	    int detectedStart = (startRow == 0) ? findRowNumber(sheet) : startRow;
	    if (detectedStart > lastRowNum + 10) detectedStart = 5;
	    int finalStartRow = Math.max(detectedStart, 0);
	    int finalEndRow = (endRow == 0) ? lastRowNum : Math.min(endRow, lastRowNum);
	    if (finalStartRow > finalEndRow) {
	        return new ArrayList<>();
	    }
	    List<ExcelExtractBOQ> extractedData = new ArrayList<>();
	    int serialNumber = 1;
	    for (int rowNum = finalStartRow; rowNum <= finalEndRow; rowNum++) {
	        Row row = sheet.getRow(rowNum);
	        if (row == null) continue;
	        ExcelExtractBOQ boq = new ExcelExtractBOQ();
	        boq.setSno(serialNumber++);
	        boolean hasData = false;
	        for (Map.Entry<String, String> entry : columnMapping.entrySet()) {
	            String internalField = entry.getKey();
	            String columnHeader = entry.getValue();
	            Integer colIndex = columnIndex.get(columnHeader);
	            if (colIndex == null) continue;
	            Cell cell = row.getCell(colIndex);
	            String rawValue = getCellValueAsString(cell, evaluator);
	            switch (internalField) {
	                case "boqCode" -> { boq.setBoqCode(rawValue); if(isNotEmpty(rawValue)) hasData=true; }
	                case "boqName" -> { boq.setBoqName(rawValue); if(isNotEmpty(rawValue)) hasData=true; }
	                case "quantity" -> {
	                    if (rawValue != null && !rawValue.isBlank()) {
	                        try {
	                            boq.setQuantity(Double.parseDouble(rawValue.trim()));
	                            hasData = true;
	                        } catch (NumberFormatException e) {
	                            
	                        }
	                    }
	                }
	                case "uom" -> { boq.setUom(rawValue); if(rawValue!=null) hasData=true; }
	            }
	        }
	        boq.setLastLevel(boq.getQuantity() > 0 || (boq.getUom() != null && !boq.getUom().isBlank()));
	        if (hasData || !columnIndex.isEmpty()) {  
	            extractedData.add(boq);
	        }
	    }
	    return extractedData;
	}

	public byte[] generateQuoteAttachement(Set<BOQDto> boqDtos) {
		try (Workbook workbook = new XSSFWorkbook();
			 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

			Sheet sheet = workbook.createSheet("BOQ Details");

			CellStyle lockedStyle = workbook.createCellStyle();
			lockedStyle.setLocked(true);

			CellStyle editableStyle = workbook.createCellStyle();
			editableStyle.setLocked(false);
			editableStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
			editableStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			editableStyle.setBorderBottom(BorderStyle.THIN);
			editableStyle.setBorderTop(BorderStyle.THIN);
			editableStyle.setBorderRight(BorderStyle.THIN);
			editableStyle.setBorderLeft(BorderStyle.THIN);

			CellStyle headerStyle = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setBold(true);
			headerStyle.setFont(font);
			headerStyle.setLocked(true);

			Row headerRow = sheet.createRow(0);
			String[] headers = {"Boq Code", "Boq Name", "Unit", "Quantity", "Rate"};

			for (int i = 0; i < headers.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(headers[i]);
				cell.setCellStyle(headerStyle);
			}

			int rowIdx = 1;
			if (boqDtos != null) {
				for (BOQDto dto : boqDtos) {
					Row row = sheet.createRow(rowIdx++);

					Cell c0 = row.createCell(0);
					c0.setCellValue(dto.getBoqCode() != null ? dto.getBoqCode() : "");
					c0.setCellStyle(lockedStyle);

					Cell c1 = row.createCell(1);
					c1.setCellValue(dto.getBoqName() != null ? dto.getBoqName() : "");
					c1.setCellStyle(lockedStyle);

					Cell c2 = row.createCell(2);
					c2.setCellValue(dto.getUom() != null ? dto.getUom().getUomCode() : "");
					c2.setCellStyle(lockedStyle);

					Cell c3 = row.createCell(3);
					c3.setCellValue(dto.getQuantity() != 0 ? dto.getQuantity() : 0);
					c3.setCellStyle(lockedStyle);

					Cell c4 = row.createCell(4);
					c4.setCellValue("");
					c4.setCellStyle(editableStyle);
				}
			}

			for (int i = 0; i < headers.length; i++) {
				sheet.autoSizeColumn(i);
			}

			sheet.protectSheet("password");

			workbook.write(out);
			return out.toByteArray();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public List<BOQSummaryDTO> getExtractedTenderData(MultipartFile file) {
		List<BOQSummaryDTO> boqList = new ArrayList<>();
		DataFormatter dataFormatter = new DataFormatter();
		try (InputStream inputStream = file.getInputStream();
			 Workbook workbook = WorkbookFactory.create(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null) continue;
				Cell codeCell = row.getCell(0);
				if (codeCell == null || codeCell.getCellType() == CellType.BLANK) continue;
				BOQSummaryDTO dto = new BOQSummaryDTO();
				dto.setBoqCode(dataFormatter.formatCellValue(row.getCell(0)));
				dto.setBoqName(dataFormatter.formatCellValue(row.getCell(1)));
				dto.setUnit(dataFormatter.formatCellValue(row.getCell(2)));
				String quantityStr = dataFormatter.formatCellValue(row.getCell(3));
				String rateStr = dataFormatter.formatCellValue(row.getCell(4));
				double quantity = 0.0;
				double rate = 0.0;
				try {
					if (quantityStr != null && !quantityStr.trim().isEmpty()) {
						quantity = Double.parseDouble(quantityStr.replace(",", ""));
					}
				} catch (NumberFormatException ignored) {
				}

				try {
					if (rateStr != null && !rateStr.trim().isEmpty()) {
						rate = Double.parseDouble(rateStr.replace(",", ""));
					}
				} catch (NumberFormatException ignored) {
				}
				dto.setQuantity(quantity);
				dto.setRate(rate);
				dto.setTotalAmount(quantity * rate);
				boqList.add(dto);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to process Excel file", e);
		}

		return boqList;
	}
}
