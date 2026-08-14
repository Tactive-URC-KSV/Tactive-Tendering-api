package com.ksv.tactive.fileservice;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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
import com.ksv.tactive.project.entity.Uom;
import com.ksv.tactive.project.entity.BOQ;
import com.ksv.tactive.service.MasterService;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class ExcelFileService {

	@Autowired
	private MasterService masterService;

	private Workbook readXlsxFile(MultipartFile file) throws IOException {
		InputStream inputStream = file.getInputStream();
		return new XSSFWorkbook(inputStream);
	}

	private Workbook readXlsFile(MultipartFile file) throws IOException {
		InputStream inputStream = file.getInputStream();
		return new HSSFWorkbook(inputStream);
	}

	public List<String> getSheetNames(MultipartFile file) throws IOException {
		Workbook workbook = file.getOriginalFilename().endsWith(".xlsx") ? readXlsxFile(file) : readXlsFile(file);
		List<String> sheets = new ArrayList<>();
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			sheets.add(workbook.getSheetName(i));
		}
		return sheets;
	}

	public List<String> getColumns(MultipartFile boqFile, String sheetName) throws IOException {
		Workbook workbook = boqFile.getOriginalFilename().endsWith(".xlsx") ? readXlsxFile(boqFile) : readXlsFile(boqFile);
		Sheet sheet = workbook.getSheet(sheetName);
		return getColumnHeader(sheet);
	}

	private List<String> getColumnHeader(Sheet sheet) {
		List<String> columns = new ArrayList<>();
		if (sheet == null) return columns;
		DataFormatter formatter = new DataFormatter();

		// Limit search to first 50 rows to avoid scanning entire empty sheets
		for (int i = sheet.getFirstRowNum(); i <= Math.min(sheet.getLastRowNum(), 50); i++) {
			Row row = sheet.getRow(i);
			if (row == null || row.getZeroHeight()) continue; // Skip hidden rows

			columns.clear();
			for (int j = Math.max(0, row.getFirstCellNum()); j < row.getLastCellNum(); j++) {
				if (sheet.isColumnHidden(j)) continue; // Skip hidden columns

				Cell cell = row.getCell(j);
				String value = formatter.formatCellValue(cell);
				if (value != null && !value.trim().isEmpty()) {
					columns.add(value.trim());
				}
			}
			if (columns.size() >= 3) {
				return columns;
			}
		}
		return columns;
	}

	public List<BOQ> mapBOQData(String sheetName, Map<String, List<String>> columnMapping, InputStream inputStream)
			throws EncryptedDocumentException, IOException, DataNotFoundException {
		Sheet sheet = findSheetByName(sheetName, inputStream);
		Map<String, List<Integer>> columnIndex = findColumnIndexMap(sheet, columnMapping);
		List<BOQ> boqList = new ArrayList<>();

		if (sheet != null && !columnIndex.isEmpty()) {
			FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
			int rowNum = findRowNumber(sheet, columnMapping);

			for (int row = rowNum; row <= sheet.getLastRowNum(); row++) {
				Row currentRow = sheet.getRow(row);
				// Skip null and hidden rows
				if (currentRow != null && !currentRow.getZeroHeight()) {
					BOQ boq = new BOQ();
					boolean hasData = false;

					for (Map.Entry<String, List<Integer>> entry : columnIndex.entrySet()) {
						String internalField = entry.getKey();
						List<Integer> colIndices = entry.getValue();

						// Collect all non-null values from mapped columns
						List<String> values = new ArrayList<>();
						for (Integer colIdx : colIndices) {
							Cell cell = currentRow.getCell(colIdx);
							String value = getCellValueAsString(cell, evaluator);
							if (value != null && !value.isBlank()) {
								values.add(value);
							}
						}

						if (values.isEmpty()) continue;

						switch (internalField) {
							case "boqCode" -> { boq.setBoqCode(String.join(" ", values)); hasData = true; }
							case "boqName" -> { boq.setBoqName(String.join(" ", values)); hasData = true; }
							case "division" -> { boq.setDivision(String.join(" ", values)); hasData = true; }
							case "quantity" -> {
								for (String val : values) {
									try {
										boq.setQuantity(Double.valueOf(val.replaceAll("[^0-9.-]", "")));
										boq.setLastLevel(true);
										hasData = true;
										break;
									} catch (NumberFormatException ignored) {}
								}
							}
							case "uom" -> {
								boq.setLastLevel(true);
								hasData = true;
							}
							case "pageNo" -> {
								for (String val : values) {
									try {
										String cleanedValue = val.replace(",", "").replaceAll("[^0-9.-]", "").trim();
										if (!cleanedValue.isEmpty()) {
											boq.setPageNo(Double.valueOf(cleanedValue).longValue());
											hasData = true;
											break;
										}
									} catch (Exception e) {}
								}
							}
						}
					}
					if(hasData) {
						boqList.add(boq);
					}
				}
			}
		}
		return boqList;
	}

	private boolean isNotEmpty(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String getCellValueAsString(Cell cell, FormulaEvaluator evaluator) {
		if (cell == null) return null;
		try {
			DataFormatter formatter = new DataFormatter();
			String value = formatter.formatCellValue(cell, evaluator).trim();
			if (value.isEmpty()) return null;
			return value.replace("\n", " ").trim();
		} catch (Exception e) {
			return null;
		}
	}

	private int findRowNumber(Sheet sheet, Map<String, List<String>> columnMapping) {
		DataFormatter formatter = new DataFormatter();
		// Flatten all column header values
		List<String> allExpectedHeaders = columnMapping.values().stream()
				.flatMap(List::stream)
				.toList();
		for (int row = sheet.getFirstRowNum(); row <= Math.min(sheet.getLastRowNum(), 50); row++) {
			Row currentRow = sheet.getRow(row);
			if (currentRow != null && !currentRow.getZeroHeight()) {
				int matchCount = 0;
				for (int cell = currentRow.getFirstCellNum(); cell < currentRow.getLastCellNum(); cell++) {
					if (sheet.isColumnHidden(cell)) continue;

					Cell currentCell = currentRow.getCell(cell);
					String cellValue = formatter.formatCellValue(currentCell).toLowerCase().replaceAll("[^a-z0-9]", "");

					for (String expectedHeader : allExpectedHeaders) {
						String normalizedExpected = expectedHeader.toLowerCase().replaceAll("[^a-z0-9]", "");
						if (!cellValue.isEmpty() && cellValue.contains(normalizedExpected)) {
							matchCount++;
							break;
						}
					}
				}
				if (matchCount >= 2) {
					return row + 1;
				}
			}
		}
		return 0;
	}

	private Map<String, List<Integer>> findColumnIndexMap(
			Sheet sheet,
			Map<String, List<String>> columnMapping) {

		Map<String, List<Integer>> columnIndexMap = new HashMap<>();

		if (sheet == null) {
			return columnIndexMap;
		}

		DataFormatter formatter = new DataFormatter();

		for (int rowNum = sheet.getFirstRowNum();
			 rowNum <= Math.min(sheet.getLastRowNum(), 50);
			 rowNum++) {

			Row row = sheet.getRow(rowNum);

			if (row == null || row.getZeroHeight()) {
				continue;
			}

			Map<String, List<Integer>> currentRowMapping = new HashMap<>();

			int emptyCellStreak = 0;

			for (int cellNum = Math.max(0, row.getFirstCellNum());
				 cellNum < row.getLastCellNum();
				 cellNum++) {

				if (sheet.isColumnHidden(cellNum)) {
					continue;
				}

				Cell cell = row.getCell(cellNum);

				if (cell == null || cell.getCellType() == CellType.BLANK) {
					emptyCellStreak++;

					if (emptyCellStreak >= 5) {
						break;
					}

					continue;
				}

				emptyCellStreak = 0;

				String rawCellValue = formatter.formatCellValue(cell);

				if (rawCellValue == null || rawCellValue.trim().isEmpty()) {
					continue;
				}

				String cellValue = rawCellValue
						.toLowerCase()
						.replaceAll("[^a-z0-9]", "");

				for (Map.Entry<String, List<String>> mapping : columnMapping.entrySet()) {

					String internalKey = mapping.getKey();
					List<String> expectedHeaders = mapping.getValue();

					for (String expectedHeader : expectedHeaders) {

						if (expectedHeader == null || expectedHeader.trim().isEmpty()) {
							continue;
						}

						String normalizedExpected = expectedHeader
								.toLowerCase()
								.replaceAll("[^a-z0-9]", "");

						boolean exactMatch =
								cellValue.equals(normalizedExpected);

						boolean fuzzyMatch =
								cellValue.replaceAll("_", "")
										.equals(normalizedExpected.replaceAll("_", ""));

						if (exactMatch || fuzzyMatch) {

							List<Integer> indices =
									currentRowMapping.computeIfAbsent(
											internalKey,
											k -> new ArrayList<>()
									);

							if (!indices.contains(cellNum)) {
								indices.add(cellNum);
							}

							break;
						}
					}
				}
			}

			if (currentRowMapping.size() >= 2) {

				columnIndexMap.clear();
				columnIndexMap.putAll(currentRowMapping);

				if (columnIndexMap.size() == columnMapping.size()) {
					break;
				}
			}
		}
		return columnIndexMap;
	}
	private Sheet findSheetByName(String sheetName, InputStream inputStream) throws IOException {
		Workbook workbook = WorkbookFactory.create(inputStream);
		return workbook != null ? workbook.getSheet(sheetName) : null;
	}

	public void generateBoqFile(List<BOQ> boqList, HttpServletResponse response) throws IOException {
		String projectName = boqList.get(0).getProjectId().toString().trim().replaceAll("\\s+", "_");
		response.setHeader("Content-Disposition", "attachment; filename=" + projectName + ".xlsx");
		try (XSSFWorkbook workBook = new XSSFWorkbook()) {
			XSSFSheet sheet = workBook.createSheet("BOQ");
			addData(sheet, boqList);
			workBook.write(response.getOutputStream());
			response.getOutputStream().flush();
		}
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
			row.createCell(1).setCellValue(boq.getId() != null ? boq.getId().toString() : "");
			row.createCell(2).setCellValue(boq.getBoqCode() != null ? boq.getBoqCode() : "");
			row.createCell(3).setCellValue(boq.getBoqName() != null ? boq.getBoqName() : "");
			row.createCell(4).setCellValue(boq.getUom() != null ? getUOMCode(boq.getUom().getId().toString()) : "");
			row.createCell(5).setCellValue(boq.getQuantity() != 0.0 ? boq.getQuantity() : 0.0);
			row.createCell(8).setCellValue(boq.getParentBOQ() != null ? boq.getParentBOQ().getBoqCode() : "");
			row.createCell(9).setCellValue(boq.isLastLevel() ? "Yes" : "No");
		}
	}

	private String getUOMCode(String uomId) {
		Uom uom = masterService.findUomById(uomId);
		return uom != null ? uom.getUomCode() : "";
	}

	private void addHeader(XSSFSheet sheet) {
		Row row = sheet.createRow(0);
		CellStyle style = sheet.getWorkbook().createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setWrapText(true);
		row.setRowStyle(style);
		String[] header = { "S No", "ID", "BOQ Code", "BOQ Name", "Uom", "Quantity", "Rate", "Amount", "Parent Code", "Last level" };
		for (int i = 0; i < header.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(header[i]);
		}
	}

	public List<ExcelExtractBOQ> getExtractedData(
			MultipartFile boqFile, String sheetName, Map<String, List<String>> columnMapping, int startRow, int endRow, String pageBreakWord) throws IOException {

		Workbook workbook = boqFile.getOriginalFilename().endsWith(".xlsx") ? readXlsxFile(boqFile) : readXlsFile(boqFile);
		Sheet sheet = workbook.getSheet(sheetName);
		if (sheet == null) return new ArrayList<>();
		Map<String, List<Integer>> columnIndex = findColumnIndexMap(sheet, columnMapping);
		if (columnIndex.isEmpty()) return new ArrayList<>();

		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		int lastRowNum = sheet.getLastRowNum();
		int detectedStart = (startRow == 0) ? findRowNumber(sheet, columnMapping) : startRow;
		int finalStartRow = Math.max(detectedStart, 0);
		int finalEndRow = (endRow == 0) ? lastRowNum : Math.min(endRow, lastRowNum);

		List<ExcelExtractBOQ> extractedData = new ArrayList<>();
		int serialNumber = 1;
		long currentPageNo = 1L;

		for (int rowNum = finalStartRow; rowNum <= finalEndRow; rowNum++) {
			Row row = sheet.getRow(rowNum);

			// SANITIZATION: Skip null rows and hidden rows
			if (row == null || row.getZeroHeight()) continue;

			boolean pageBreakFound = false;
			boolean hasOtherText = false;
			if (pageBreakWord != null && !pageBreakWord.trim().isEmpty()) {
				String lowerBreakWord = pageBreakWord.toLowerCase().trim();
				for (int i = 0; i < row.getLastCellNum(); i++) {
					Cell cell = row.getCell(i);
					String cellValue = getCellValueAsString(cell, evaluator);
					if (cellValue != null && !cellValue.trim().isEmpty()) {
						String lowerCell = cellValue.toLowerCase().trim();
						if (lowerCell.contains(lowerBreakWord)) {
							pageBreakFound = true;
							String remaining = lowerCell.replace(lowerBreakWord, "").trim();
							if (!remaining.isEmpty()) {
								hasOtherText = true;
							}
						} else {
							hasOtherText = true;
						}
					}
				}
			}

			if (pageBreakFound && !hasOtherText) {
				currentPageNo++;
				continue;
			}

			ExcelExtractBOQ boq = new ExcelExtractBOQ();
			boq.setSno(serialNumber++);
			boq.setPageNo(currentPageNo);
			boolean hasData = false;

			for (Map.Entry<String, List<Integer>> entry : columnIndex.entrySet()) {
				String internalField = entry.getKey();
				List<Integer> colIndices = entry.getValue();

				// Collect all non-null values from mapped columns
				List<String> values = new ArrayList<>();
				for (Integer colIdx : colIndices) {
					Cell cell = row.getCell(colIdx);
					String rawValue = getCellValueAsString(cell, evaluator);
					if (rawValue != null) {
						values.add(rawValue);
					}
				}

				if (values.isEmpty()) continue;

				switch (internalField) {
					case "boqCode" -> {
						boq.setBoqCode(String.join(" -- ", values));
						hasData = true;
					}
					case "boqName" -> {
						boq.setBoqName(String.join(" -- ", values));
						hasData = true;
					}
					case "division" -> {
						boq.setDivision(String.join(" -- ", values));
						hasData = true;
					}
					case "quantity" -> {
						for (String val : values) {
							try {
								String cleanedValue = val.replace(",", "").replaceAll("[^0-9.-]", "").trim();
								if (!cleanedValue.isEmpty()) {
									boq.setQuantity(Double.parseDouble(cleanedValue));
									hasData = true;
									break; // Use first valid numeric value
								}
							} catch (Exception e) {
								// Try next column
							}
						}
					}
					case "uom" -> {
						boq.setUom(values.get(0)); // Use first non-empty UOM
						hasData = true;
					}
					case "pageNo" -> {
						for (String val : values) {
							try {
								String cleanedValue = val.replace(",", "").replaceAll("[^0-9.-]", "").trim();
								if (!cleanedValue.isEmpty()) {
									boq.setPageNo(Double.valueOf(cleanedValue).longValue());
									hasData = true;
									break;
								}
							} catch (Exception e) {}
						}
					}
				}
			}

			boq.setLastLevel(
					(boq.getQuantity() != null && boq.getQuantity() > 0)
							|| (boq.getUom() != null && !boq.getUom().isBlank())
			);

			if (hasData) {
				extractedData.add(boq);
			} else {
				serialNumber--; // Revert serial number if row was empty
			}

			if (pageBreakFound) {
				currentPageNo++;
			}
		}
		workbook.close();
		return extractedData;
	}

	public byte[] generateQuoteAttachement(Set<BOQDto> boqDtos) {
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
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
		try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);

				// Sanitize tender data too!
				if (row == null || row.getZeroHeight()) continue;

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
				} catch (NumberFormatException ignored) {}

				try {
					if (rateStr != null && !rateStr.trim().isEmpty()) {
						rate = Double.parseDouble(rateStr.replace(",", ""));
					}
				} catch (NumberFormatException ignored) {}

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