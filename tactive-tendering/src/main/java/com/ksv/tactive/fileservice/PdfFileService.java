package com.ksv.tactive.fileservice;

import com.itextpdf.text.Font;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import technology.tabula.*;
import technology.tabula.extractors.BasicExtractionAlgorithm;
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
		document.addTitle(projectName + " BOQ Report");
		Paragraph title = new Paragraph(projectName + " - BOQ Report");
		title.setAlignment(Element.ALIGN_CENTER);
		document.add(title);
		document.add(new Paragraph("\n"));
		String[] headers = { "S No", "BOQ Code", "BOQ Name", "Uom", "Quantity", "Rate", "Amount", "Parent Code",
				"Last Level" };
		float[] colWidths = { 1f, 2f, 4f, 1.5f, 2f, 2f, 3f, 2f, 2f };
		PdfPTable table = new PdfPTable(colWidths);
		table.setWidthPercentage(100);
		table.setHeaderRows(1);
		BaseColor headerBg = new BaseColor(0, 121, 182);
		for (String h : headers) {
			PdfPCell cell = new PdfPCell(new Phrase(h));
			cell.setBackgroundColor(headerBg);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setPadding(5);
			table.addCell(cell);
		}
		Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
		int slNo = 1;
		for (BOQ boq : boqList) {
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
		document.add(table);
		document.close();
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

	public List<ExcelExtractBOQ> getExtractedData(
			MultipartFile boqFile,
			Map<String, List<String>> columnMapping,
			int startRow,
			int endRow,
			int startPage
	) throws IOException {

		List<ExcelExtractBOQ> boqList = new ArrayList<>();

		try (
				InputStream inputStream = boqFile.getInputStream();
				PDDocument document = PDDocument.load(inputStream)
		) {

			ObjectExtractor extractor = new ObjectExtractor(document);

			PageIterator pages = extractor.extract();

			int currentPage = 0;

			int serialNo = 1;

			int globalRowCounter = 0;

			Pattern boqCodePattern =
					Pattern.compile(
							"^([0-9]+(\\.[0-9A-Za-z()]+)+|[A-Z]+\\.[0-9]+|[a-z]\\.)$"
					);

			/*
			 * DYNAMIC HEADER NORMALIZATION
			 */
			Set<String> normalizedMappedHeaders =
					new HashSet<>();

			for (Map.Entry<String, List<String>> mapping
					: columnMapping.entrySet()) {

				for (String header : mapping.getValue()) {

					normalizedMappedHeaders.add(
							header.toLowerCase()
									.replaceAll("[^a-z0-9]", "")
					);
				}
			}

			while (pages.hasNext()) {

				Page page = pages.next();

				currentPage++;

				if (currentPage < startPage) {
					continue;
				}

				BasicExtractionAlgorithm extractionAlgorithm =
						new BasicExtractionAlgorithm();

				List<Table> tables =
						extractionAlgorithm.extract(page);

				for (Table table : tables) {

					List<List<String>> rows =
							new ArrayList<>();

					for (List<RectangularTextContainer> tableRow
							: table.getRows()) {

						List<String> extractedRow =
								new ArrayList<>();

						for (RectangularTextContainer cell
								: tableRow) {

							String text =
									cell.getText() != null
											? cell.getText().trim()
											: "";

							extractedRow.add(text);
						}

						rows.add(extractedRow);
					}

					for (List<String> row : rows) {

						globalRowCounter++;

						if (startRow > 0
								&& globalRowCounter < startRow) {
							continue;
						}

						if (endRow > 0
								&& globalRowCounter > endRow) {

							return boqList;
						}

						if (row.isEmpty()) {
							continue;
						}

						String fullRow =
								String.join(" ", row)
										.replaceAll("\\s+", " ")
										.trim();

						if (fullRow.isBlank()) {
							continue;
						}

						String normalizedRow =
								fullRow.toLowerCase()
										.replaceAll("[^a-z0-9]", "");

						/*
						 * REMOVE HEADER ROWS
						 */
						boolean isHeaderRow =
								normalizedMappedHeaders.stream()
										.allMatch(normalizedRow::contains);

						if (isHeaderRow) {
							continue;
						}

						/*
						 * REMOVE FOOTERS
						 */
						if (normalizedRow.matches(".*page[ivxlcdm0-9]+.*")) {
							continue;
						}

						if (normalizedRow.contains("carriedtosummary")) {
							continue;
						}

						if (normalizedRow.contains("carriedtograndsummary")) {
							continue;
						}

						if (normalizedRow.contains("summary")) {
							continue;
						}

						/*
						 * SPLIT USING BOQ CODE
						 */
						List<String> tokens =
								Arrays.stream(fullRow.split("\\s+"))
										.filter(v -> !v.isBlank())
										.toList();

						List<Integer> boqPositions =
								new ArrayList<>();

						for (int i = 0; i < tokens.size(); i++) {

							Matcher matcher =
									boqCodePattern.matcher(tokens.get(i));

							if (matcher.find()) {
								boqPositions.add(i);
							}
						}

						if (boqPositions.isEmpty()) {
							continue;
						}

						/*
						 * CREATE SEPARATE BOQ ROWS
						 */
						for (int i = 0; i < boqPositions.size(); i++) {

							int start =
									boqPositions.get(i);

							int end =
									(i + 1 < boqPositions.size())
											? boqPositions.get(i + 1)
											: tokens.size();

							List<String> itemTokens =
									tokens.subList(start, end);

							if (itemTokens.isEmpty()) {
								continue;
							}

							String boqCode =
									itemTokens.get(0)
											.trim();

							String remainingText =
									itemTokens.size() > 1
											? String.join(
											" ",
											itemTokens.subList(
													1,
													itemTokens.size()
											)
									)
											: "";

							/*
							 * FIND QUANTITY
							 */
							Double quantity = null;

							Pattern quantityPattern =
									Pattern.compile(
											"([0-9]{1,3}(,[0-9]{3})*(\\.[0-9]+)?|[0-9]+(\\.[0-9]+)?)$"
									);

							Matcher quantityMatcher =
									quantityPattern.matcher(remainingText);

							if (quantityMatcher.find()) {

								try {

									String quantityText =
											quantityMatcher.group(1)
													.replace(",", "");

									quantity =
											Double.parseDouble(quantityText);

									remainingText =
											remainingText.substring(
													0,
													quantityMatcher.start()
											).trim();

								} catch (Exception ignored) {
								}
							}

							/*
							 * FIND UOM USING REGEX
							 */
							String detectedUom = "";

							Pattern uomPattern =
									Pattern.compile(
											"\\b([a-zA-Z]{1,10}(\\/[a-zA-Z]{1,10})?)\\b"
									);

							Matcher uomMatcher =
									uomPattern.matcher(remainingText);

							List<String> possibleUoms =
									new ArrayList<>();

							while (uomMatcher.find()) {

								String match =
										uomMatcher.group()
												.trim();

								if (match.length() <= 10) {
									possibleUoms.add(match);
								}
							}

							if (!possibleUoms.isEmpty()) {

								detectedUom =
										possibleUoms.get(
												possibleUoms.size() - 1
										);

								remainingText =
										remainingText.replaceAll(
														"\\b"
																+ Pattern.quote(detectedUom)
																+ "\\b",
														""
												)
												.trim();
							}

							/*
							 * REMOVE INVALID ROWS
							 */
							if (boqCode.isBlank()) {
								continue;
							}

							if (remainingText.isBlank()) {
								continue;
							}

							if (remainingText.length() < 2) {
								continue;
							}

							ExcelExtractBOQ boq =
									new ExcelExtractBOQ();

							boq.setSno(serialNo++);

							boq.setBoqCode(boqCode);

							boq.setBoqName(remainingText);

							boq.setUom(detectedUom);

							boq.setQuantity(quantity);

							boq.setLastLevel(
									!detectedUom.isBlank()
											|| quantity != null
							);

							boqList.add(boq);
						}
					}
				}
			}
		}

		return boqList;
	}
}