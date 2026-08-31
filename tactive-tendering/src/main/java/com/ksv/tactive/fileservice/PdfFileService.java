package com.ksv.tactive.fileservice;

import com.itextpdf.text.Font;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.ksv.tactive.dto.ExcelExtractBOQ;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.project.entity.Uom;
import com.ksv.tactive.project.entity.BOQ;
import com.ksv.tactive.service.MasterService;

import jakarta.servlet.http.HttpServletResponse;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

@Service
public class PdfFileService {
	@Autowired
	private MasterService masterService;

	private PageIterator getPages(InputStream boqFile) throws IOException {
		PDDocument document = PDDocument.load(boqFile);
		ObjectExtractor oe = new ObjectExtractor(document);
		return oe.extract();
	}

	public List<String> getPdfPages(MultipartFile boqFile) throws IOException {
		InputStream inputStream = boqFile.getInputStream();
		PDDocument document = PDDocument.load(inputStream);
		int totalPages = document.getNumberOfPages();
		document.close();
		List<String> pages = new ArrayList<>();
		for (int i = 1; i <= totalPages; i++) {
			pages.add(String.valueOf(i));
		}
		return pages;
	}

	public List<String> getColumnsHeaders(MultipartFile boqFile, int startPage) throws IOException {
		InputStream inputStream = boqFile.getInputStream();
		PDDocument document = PDDocument.load(inputStream);

		ObjectExtractor oe = new ObjectExtractor(document);
		PageIterator iterator = oe.extract();

		List<List<String>> allRows = new ArrayList<>();
		List<String> headers = null;
		int currentPageNumber = 0;

		while (iterator.hasNext()) {
			Page page = iterator.next();
			currentPageNumber++;

			if (currentPageNumber < startPage) continue;

			if (page == null) continue;

			SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
			List<Table> tables = sea.extract(page);

			for (Table table : tables) {
				List<List<String>> rows = table.getRows().stream()
						.map(row -> row.stream().map(cell -> cell.getText()).toList())
						.toList();

				if (rows.isEmpty()) continue;

				if (headers == null) {
					headers = rows.get(0);
				}

				int startIndex = rows.get(0).equals(headers) ? 1 : 0;

				for (int i = startIndex; i < rows.size(); i++) {
					allRows.add(rows.get(i));
				}
			}
		}

		document.close();

		return headers;
	}
	public List<BOQ> mapBOQData(Map<String, String> columnMapping, InputStream inputStream, int startPage)
			throws IOException, DataNotFoundException {
		List<BOQ> boqList = new ArrayList<>();
		PDDocument document = PDDocument.load(inputStream);
		try {
			ObjectExtractor oe = new ObjectExtractor(document);
			PageIterator iterator = oe.extract();
			List<String> headers = null;
			int currentPageNumber = 0;
			while (iterator.hasNext()) {
				Page page = iterator.next();
				currentPageNumber++;
				if (currentPageNumber < startPage) continue;

				SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
				List<Table> tables = sea.extract(page);
				for (Table table : tables) {
					List<List<String>> rows = table.getRows().stream()
							.map(row -> row.stream()
									.map(cell -> cell.getText() != null
											? cell.getText().replaceAll("[\\r\\n]+", " ").trim()
											: "")
									.toList())
							.toList();
					if (rows.isEmpty()) continue;
					if (headers == null || headers.isEmpty()) {
						headers = rows.get(0).stream()
								.map(String::trim)
								.map(String::toLowerCase)
								.toList();
					}
					int startIndex = (rows.get(0).equals(headers)) ? 1 : 0;
					for (int i = startIndex; i < rows.size(); i++) {
						List<String> row = rows.get(i);
						if (row.stream().allMatch(String::isBlank)) continue;
						String joinedRow = String.join(" ", row).toLowerCase();
						if (joinedRow.contains("total") || joinedRow.contains("grand total")) continue;
						BOQ boq = new BOQ();
						boolean hasValidData = false;
						for (Map.Entry<String, String> entry : columnMapping.entrySet()) {
							String internalField = entry.getKey();
							String columnHeader = entry.getValue().trim().toLowerCase();
							int colIndex = -1;
							for (int idx = 0; idx < headers.size(); idx++) {
								if (headers.get(idx).equalsIgnoreCase(columnHeader)
										|| headers.get(idx).contains(columnHeader)
										|| columnHeader.contains(headers.get(idx))) {
									colIndex = idx;
									break;
								}
							}
							if (colIndex == -1 || colIndex >= row.size()) continue;
							String value = row.get(colIndex).trim();
							if (value.isEmpty()) continue;
							switch (internalField) {
								case "boqCode" -> {
									boq.setBoqCode(value);
									hasValidData = true;
								}
								case "boqName" -> {
									boq.setBoqName(value);
									hasValidData = true;
								}
								case "division" -> {
									boq.setDivision(value);
									hasValidData = true;
								}
								case "quantity" -> {
									Double parsedQty = parseQuantity(value);
									if (parsedQty != null) {
										boq.setQuantity(parsedQty);
										hasValidData = true;
									}
								}
								case "uom" -> {
									if (!value.isBlank()) {
//	                                boq.setUom(excelFileService.findUOM(value));
										boq.setLastLevel(true);
										hasValidData = true;
									} else {
										boq.setLastLevel(false);
									}
								}
								case "pageNo" -> {
									try {
										String cleanedValue = value.replace(",", "").replaceAll("[^0-9.-]", "").trim();
										if (!cleanedValue.isEmpty()) {
											boq.setPageNo(Double.valueOf(cleanedValue).longValue());
											hasValidData = true;
										}
									} catch (Exception e) {}
								}
							}
						}
						if (hasValidData) boqList.add(boq);
					}
				}
			}
		} finally {
			document.close();
		}
		return boqList;
	}
	private Double parseQuantity(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			String cleaned = value
					.replace(",", "")
					.replace("\n", " ")
					.replaceAll("[^0-9.\\-]", "")
					.trim();
			if (cleaned.isEmpty()) {
				return null;
			}
			return Double.parseDouble(cleaned);
		} catch (Exception e) {
			try {
				java.util.regex.Matcher matcher =
						java.util.regex.Pattern
								.compile("[-+]?[0-9]*\\.?[0-9]+")
								.matcher(value);
				if (matcher.find()) {
					return Double.parseDouble(matcher.group());
				}
			} catch (Exception ignored) {
			}
		}
		return null;
	}
	public void generateBoqPdf(List<BOQ> boqList, HttpServletResponse response) throws IOException, DocumentException {
		String projectName = boqList.get(0).getProjectId().toString().trim().replaceAll("\\s+", "_");
		String fileName = projectName + "_BOQ.pdf";
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		Document document = new Document(PageSize.A4, 20, 20, 20, 20);
		PdfWriter.getInstance(document, response.getOutputStream());
		document.open();
		document.addTitle("BOQ Report");
		Paragraph title = new Paragraph("BOQ Report");
		title.setAlignment(Element.ALIGN_CENTER);
		document.add(title);
		document.add(new Paragraph("\n"));
		String[] headers = { "S No", "BOQ Code", "BOQ Name", "Uom", "Quantity", "Rate", "Amount", "Parent Code",
				"Last Level" };
		float[] colWidths = { 1f, 2f, 4f, 1.5f, 2f, 2f, 3f, 2f, 2f };
		BaseColor headerBg = new BaseColor(0, 121, 182);
		Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
		Font boldRowFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
		int slNo = 1;

		if (boqList.isEmpty()) {
			PdfPTable table = createBoqTable(headers, colWidths, headerBg);
			document.add(table);
		} else {
			Map<Long, List<BOQ>> pageWiseBoq = boqList.stream()
					.filter(boq -> boq.getPageNo() != null)
					.collect(java.util.stream.Collectors.groupingBy(
							BOQ::getPageNo,
							TreeMap::new,
							java.util.stream.Collectors.toList()
					));

			boolean firstPage = true;
			for (Map.Entry<Long, List<BOQ>> entry : pageWiseBoq.entrySet()) {
				List<BOQ> pageBoqs = entry.getValue();

				pageBoqs.sort((b1, b2) -> {
					if (b1.getCreatedAt() == null && b2.getCreatedAt() == null) return 0;
					if (b1.getCreatedAt() == null) return 1;
					if (b2.getCreatedAt() == null) return -1;
					return b1.getCreatedAt().compareTo(b2.getCreatedAt());
				});

				if (!firstPage) {
					document.newPage();
				}
				firstPage = false;

				PdfPTable table = createBoqTable(headers, colWidths, headerBg);
				double pageTotalAmount = 0.0;

				for (BOQ boq : pageBoqs) {
					pageTotalAmount += boq.getTotalAmount();
					String[] rowData = { String.valueOf(slNo++), safe(boq.getBoqCode()),
							safe(boq.getBoqName()), boq.getUom() != null ? getUomCode(boq.getUom().getId().toString()) : "",
							String.valueOf(boq.getQuantity()), String.valueOf(boq.getTotalRate()),
							String.valueOf(boq.getTotalAmount()),
							boq.getParentBOQ() != null ? boq.getParentBOQ().getBoqCode() : "",
							boq.isLastLevel() ? "Yes" : "No" };
					for (int i = 0; i < rowData.length; i++) {
						PdfPCell cell = new PdfPCell(new Phrase(rowData[i], rowFont));
						cell.setPadding(4);
						if (i == 3) {
							cell.setNoWrap(false);
						} else {
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						}
						table.addCell(cell);
					}
				}

				PdfPCell totalLabelCell = new PdfPCell(new Phrase("Page Total", boldRowFont));
				totalLabelCell.setColspan(6);
				totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				totalLabelCell.setPadding(4);
				table.addCell(totalLabelCell);
				
				PdfPCell totalAmountCell = new PdfPCell(new Phrase(String.format("%.2f", pageTotalAmount), boldRowFont));
				totalAmountCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				totalAmountCell.setPadding(4);
				table.addCell(totalAmountCell);
				
				PdfPCell emptyCell = new PdfPCell(new Phrase("", boldRowFont));
				emptyCell.setColspan(2);
				table.addCell(emptyCell);

				document.add(table);
			}
		}
		document.close();
	}

	private PdfPTable createBoqTable(String[] headers, float[] colWidths, BaseColor headerBg) {
		PdfPTable table = new PdfPTable(colWidths);
		table.setWidthPercentage(100);
		table.setHeaderRows(1);
		for (String h : headers) {
			PdfPCell cell = new PdfPCell(new Phrase(h));
			cell.setBackgroundColor(headerBg);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setPadding(5);
			table.addCell(cell);
		}
		return table;
	}
	private String getUomCode(String uomId) {
		Uom uom = masterService.findUomById(uomId);
		if (uom != null) {
			return uom.getUomCode();
		}
		return null;
	}
	private String safe(String val) {
		return val == null ? "" : val;
	}

	public List<ExcelExtractBOQ> getExtractedData(MultipartFile boqFile,
												  Map<String, List<String>> columnMapping,
												  int startRow,
												  int endRow,
												  int startPage,
												  String pageBreakWord) throws IOException {

		List<ExcelExtractBOQ> boqList = new ArrayList<>();
		Map<String, List<Integer>> mappedColumnIndexes = new HashMap<>();
		List<String> normalizedHeaders = null;

		try (InputStream inputStream = boqFile.getInputStream();
			 PDDocument document = PDDocument.load(inputStream)) {

			ObjectExtractor oe = new ObjectExtractor(document);
			PageIterator iterator = oe.extract();

			int globalRowCounter = 0;
			int serialNumber = 1;
			int currentPageNumber = 0;
			long currentBoqPageNo = 1L;

			while (iterator.hasNext()) {

				Page page = iterator.next();
				currentPageNumber++;

				if (currentPageNumber < startPage) {
					continue;
				}

				SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
				List<Table> tables = sea.extract(page);

				for (Table table : tables) {

					List<List<String>> rows = new ArrayList<>();

					for (var tableRow : table.getRows()) {

						List<String> rawCells = tableRow.stream()
								.map(cell -> cell.getText() != null
										? cell.getText().trim()
										: "")
								.toList();

						int maxSplit = 1;

						List<List<String>> splitColumns = new ArrayList<>();

						for (String cell : rawCells) {

							List<String> parts = Arrays.stream(cell.split("\\r?\\n"))
									.map(String::trim)
									.filter(s -> !s.isBlank())
									.toList();

							if (parts.isEmpty()) {
								parts = List.of("");
							}

							splitColumns.add(parts);

							maxSplit = Math.max(maxSplit, parts.size());
						}

						for (int i = 0; i < maxSplit; i++) {

							List<String> splitRow = new ArrayList<>();

							for (List<String> columnParts : splitColumns) {

								if (i < columnParts.size()) {
									splitRow.add(columnParts.get(i));
								} else {
									splitRow.add("");
								}
							}

							rows.add(splitRow);
						}
					}

					if (rows.isEmpty()) {
						continue;
					}

					if (normalizedHeaders == null || normalizedHeaders.isEmpty()) {

						normalizedHeaders = rows.get(0).stream()
								.map(h -> h.toLowerCase().replaceAll("[^a-z0-9]", ""))
								.toList();

						for (Map.Entry<String, List<String>> mapping : columnMapping.entrySet()) {

							String internalKey = mapping.getKey();
							List<String> expectedHeaders = mapping.getValue();

							for (String expectedHeader : expectedHeaders) {

								String normalizedExpected = expectedHeader
										.toLowerCase()
										.replaceAll("[^a-z0-9]", "");

								for (int i = 0; i < normalizedHeaders.size(); i++) {

									String actualHeader = normalizedHeaders.get(i);

									if (!actualHeader.isEmpty()
											&& (actualHeader.contains(normalizedExpected)
											|| normalizedExpected.contains(actualHeader))) {

										List<Integer> indices =
												mappedColumnIndexes.computeIfAbsent(
														internalKey,
														k -> new ArrayList<>());

										if (!indices.contains(i)) {
											indices.add(i);
										}

										break;
									}
								}
							}
						}
					}

					int startIndex = 0;

					List<String> currentRowNormalized = rows.get(0).stream()
							.map(h -> h.toLowerCase().replaceAll("[^a-z0-9]", ""))
							.toList();

					long matchCount = currentRowNormalized.stream()
							.filter(normalizedHeaders::contains)
							.count();

					if (matchCount >= 2) {
						startIndex = 1;
					}

					for (int i = startIndex; i < rows.size(); i++) {

						List<String> row = rows.get(i);

						globalRowCounter++;

						if (startRow > 0 && globalRowCounter < startRow) {
							continue;
						}

						if (endRow > 0 && globalRowCounter > endRow) {
							return boqList;
						}

						if (row.stream().allMatch(String::isBlank)) {
							continue;
						}

						List<String> normalizedRow = row.stream()
								.map(v -> v.toLowerCase().replaceAll("[^a-z0-9]", ""))
								.toList();

						boolean isHeaderRow = normalizedHeaders.stream()
								.filter(h -> !h.isBlank())
								.allMatch(normalizedRow::contains);

						if (isHeaderRow) {
							continue;
						}

						String joinedRow = String.join(" ", row).toLowerCase();

						if (joinedRow.matches(".*page\\s+[ivxlcdm\\-]+.*")) {
							continue;
						}

						if (joinedRow.contains("carried to summary")
								|| joinedRow.contains("carried to grand summary")
								|| joinedRow.contains("summary")) {
							continue;
						}

						if (joinedRow.contains("item no")
								&& joinedRow.contains("description")
								&& joinedRow.contains("unit")) {
							continue;
						}

						if (joinedRow.contains("total")
								|| joinedRow.contains("grand total")) {
							continue;
						}

						boolean pageBreakFound = false;
						boolean hasOtherText = false;
						if (pageBreakWord != null && !pageBreakWord.trim().isEmpty()) {
							String lowerBreakWord = pageBreakWord.toLowerCase().trim();
							for (String cellValue : row) {
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
							currentBoqPageNo++;
							continue;
						}

						ExcelExtractBOQ boq = new ExcelExtractBOQ();

						boq.setSno(serialNumber);
						boq.setPageNo(currentBoqPageNo);

						boolean hasValidData = false;

						for (Map.Entry<String, List<Integer>> entry : mappedColumnIndexes.entrySet()) {

							String internalField = entry.getKey();
							List<Integer> colIndices = entry.getValue();

							List<String> values = new ArrayList<>();

							for (int colIndex : colIndices) {

								if (colIndex >= row.size()) {
									continue;
								}

								String value = row.get(colIndex).trim();

								if (!value.isEmpty()) {
									values.add(value);
								}
							}

							if (values.isEmpty()) {
								continue;
							}

							switch (internalField) {

								case "boqCode" -> {
									boq.setBoqCode(String.join(" ", values));
									hasValidData = true;
								}

								case "boqName" -> {
									boq.setBoqName(String.join(" ", values));
									hasValidData = true;
								}

								case "division" -> {
									boq.setDivision(String.join(" ", values));
									hasValidData = true;
								}

								case "quantity" -> {

									for (String val : values) {

										Double parsedQty = parseQuantity(val);

										if (parsedQty != null) {
											boq.setQuantity(parsedQty);
											hasValidData = true;
											break;
										}
									}
								}

								case "uom" -> {

									String uomValue = values.get(0);

									if (!uomValue.isBlank()) {
										boq.setUom(uomValue);
										hasValidData = true;
									}
								}

								case "pageNo" -> {
									for (String val : values) {
										try {
											String cleanedValue = val.replace(",", "").replaceAll("[^0-9.-]", "").trim();
											if (!cleanedValue.isEmpty()) {
												boq.setPageNo(Double.valueOf(cleanedValue).longValue());
												hasValidData = true;
												break;
											}
										} catch (Exception e) {}
									}
								}
							}
						}

						boq.setLastLevel(
								(boq.getQuantity() != null && boq.getQuantity() > 0)
										|| (boq.getUom() != null
										&& !boq.getUom().isBlank())
						);

						if (hasValidData) {
							boqList.add(boq);
							serialNumber++;
						}

						if (pageBreakFound) {
							currentBoqPageNo++;
						}
					}
				}
			}
		}

		return boqList;
	}
}