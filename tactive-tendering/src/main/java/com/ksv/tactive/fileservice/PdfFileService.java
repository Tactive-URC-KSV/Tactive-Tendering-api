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
import org.apache.pdfbox.text.PDFTextStripper;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

@Service
public class PdfFileService {
	@Autowired
	private MasterService masterService;

	@Autowired
	private HybridPdfExtractionEngine hybridPdfExtractionEngine;

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

	public List<String> getColumnsHeaders(MultipartFile boqFile, int startPage, Integer requestedEndPage) throws IOException {
		try (InputStream inputStream = boqFile.getInputStream();
			 PDDocument document = PDDocument.load(inputStream)) {
			hybridPdfExtractionEngine.validatePdfDocument(document);
			int endPage = validatePageRange(document, startPage, requestedEndPage);

			PageIterator iterator = new ObjectExtractor(document).extract();
			List<String> headers = null;
			int currentPageNumber = 0;

			while (iterator.hasNext()) {
				Page page = iterator.next();
				currentPageNumber++;
				if (currentPageNumber < startPage) continue;
				if (currentPageNumber > endPage) break;
				if (page == null) continue;

				try {
					List<List<String>> rows = hybridPdfExtractionEngine.extractTableRows(document, page, currentPageNumber);
					List<List<String>> tableRows = rowsFromBoqHeader(rows);
					if (!tableRows.isEmpty()) {
						headers = tableRows.get(0);
						break;
					}
				} catch (Exception e) {
					throw new IOException("Failed to extract BOQ table headers from PDF page "
							+ currentPageNumber + ".", e);
				}
			}
			return headers == null ? List.of() : headers;
		}
	}

	private static final java.util.regex.Pattern CODE_IN_DESC = java.util.regex.Pattern.compile(
			"(?i)^([a-z]{1,3}|\\d+(?:\\.\\d+)*)[.)\\s]+(.*)$");

	private static boolean isValidExtractedCode(
			String code, String restDesc, String uom, String qty) {
		if (code == null || code.isBlank() || restDesc == null || restDesc.isBlank()) {
			return false;
		}
		String cleanCode = code.trim();
		boolean hasData = (uom != null && !uom.isBlank() && !uom.matches("[-–—]+"))
				|| (qty != null && !qty.isBlank() && !qty.matches("[-–—]+"));
		if (cleanCode.matches("^\\d+(\\.\\d+)*$")) {
			if (cleanCode.contains(".")) {
				return true;
			}
			if (!hasData) {
				return false;
			}
			String lowerRest = restDesc.toLowerCase(Locale.ROOT);
			if (lowerRest.matches("^(?:mm|cm|m|m2|m3|gauge|guage|coat|coats|layer|layers|set|sets|nos|no|times|inch|inches|dia|diameter|thick|ply|micron|microns|bar|bars)\\b.*")) {
				return false;
			}
			try {
				if (Long.parseLong(cleanCode) > 999) {
					return false;
				}
			} catch (Exception ignored) {}
			return true;
		}
		if (cleanCode.matches("^[B-Zb-z]$")) {
			return true;
		}
		if (cleanCode.equalsIgnoreCase("A")) {
			boolean startsWithCapital = restDesc.matches("^[A-Z].*");
			return hasData || startsWithCapital;
		}
		return false;
	}

	private static final Set<String> COMMON_HEADER_KEYWORDS = Set.of(
			"description", "desc", "item", "particular", "scope", "work",
			"qty", "quantity", "unit", "uom", "rate", "price", "amount",
			"total", "cost", "code", "sno", "srno", "no", "spec", "specification", "remark"
	);

	static List<List<String>> nonEmptyRows(List<List<String>> rows) {
		return rows.stream()
				.filter(row -> row.stream().anyMatch(cell -> cell != null && !cell.isBlank()))
				.toList();
	}

	static List<List<String>> rowsFromBoqHeader(List<List<String>> rows) {
		List<List<String>> populatedRows = nonEmptyRows(rows);
		if (populatedRows.isEmpty()) {
			return List.of();
		}

		int bestKeywordRowIndex = -1;
		int maxKeywordScore = 0;

		int searchLimit = Math.min(populatedRows.size(), 15);
		for (int i = 0; i < searchLimit; i++) {
			List<String> row = populatedRows.get(i);
			long nonBlankCellCount = row.stream().filter(c -> c != null && !c.isBlank()).count();
			if (nonBlankCellCount < 2) {
				continue;
			}

			String headerText = row.stream()
					.filter(Objects::nonNull)
					.map(value -> value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", ""))
					.reduce("", String::concat);

			int keywordScore = 0;
			for (String keyword : COMMON_HEADER_KEYWORDS) {
				if (headerText.contains(keyword)) {
					keywordScore++;
				}
			}

			if (keywordScore > maxKeywordScore) {
				maxKeywordScore = keywordScore;
				bestKeywordRowIndex = i;
			}
		}

		// If a row matched common header keywords with multiple columns
		if (bestKeywordRowIndex >= 0 && maxKeywordScore >= 1) {
			return populatedRows.subList(bestKeywordRowIndex, populatedRows.size());
		}

		// If no common English keywords matched, don't restrict:
		// Find the first row with multiple populated columns (>= 2) that is not purely numeric
		for (int i = 0; i < searchLimit; i++) {
			List<String> row = populatedRows.get(i);
			long nonBlankCount = row.stream().filter(c -> c != null && !c.isBlank()).count();
			if (nonBlankCount >= 2 && !isNumericDataRow(row)) {
				return populatedRows.subList(i, populatedRows.size());
			}
		}

		// Fallback: return populated rows starting from index 0
		return populatedRows;
	}

	private static boolean isNumericDataRow(List<String> row) {
		int numericCells = 0;
		int textCells = 0;
		for (String cell : row) {
			if (cell == null || cell.isBlank()) continue;
			String trimmed = cell.trim().replaceAll("[,%]", "");
			if (trimmed.matches("^-?\\d+(\\.\\d+)?$")) {
				numericCells++;
			} else {
				textCells++;
			}
		}
		return numericCells > textCells && numericCells >= 2;
	}

	private static boolean matchesSemanticField(String internalKey, String actualHeader) {
		if (actualHeader == null || actualHeader.isBlank()) return false;
		return switch (internalKey) {
			case "boqCode" -> actualHeader.equals("itemno") || actualHeader.equals("srno")
					|| actualHeader.equals("sno") || actualHeader.equals("no")
					|| actualHeader.equals("code") || actualHeader.contains("itemno")
					|| actualHeader.contains("srno") || actualHeader.contains("item");
			case "boqName" -> actualHeader.contains("desc") || actualHeader.contains("particular");
			case "quantity" -> actualHeader.contains("qty") || actualHeader.contains("quant");
			case "uom" -> actualHeader.contains("unit") || actualHeader.contains("uom");
			case "rateOnly" -> actualHeader.contains("rate") || actualHeader.contains("price");
			default -> false;
		};
	}

	private static boolean isHeaderMatch(List<String> row, List<String> normalizedHeaders) {
		if (row == null || normalizedHeaders == null || normalizedHeaders.isEmpty()) {
			return false;
		}
		List<String> normalizedRow = row.stream()
				.filter(Objects::nonNull)
				.map(v -> v.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", ""))
				.toList();
		long matchCount = 0;
		long validHeaderCount = 0;
		for (String header : normalizedHeaders) {
			if (header != null && !header.isBlank() && header.length() > 1) {
				validHeaderCount++;
				if (normalizedRow.contains(header)) {
					matchCount++;
				}
			}
		}
		return validHeaderCount > 0 && matchCount >= Math.min(2, validHeaderCount)
				&& ((double) matchCount / validHeaderCount >= 0.5);
	}

	static List<List<String>> expandMergedRows(List<List<String>> rows) {
		if (rows == null || rows.isEmpty()) {
			return rows;
		}
		List<List<String>> result = new ArrayList<>();
		result.add(rows.get(0));
		for (int r = 1; r < rows.size(); r++) {
			List<String> row = new ArrayList<>(rows.get(r));
			if (row.size() < 2 || row.get(1) == null || row.get(1).isBlank()) {
				result.add(row);
				continue;
			}

			String desc = row.get(1).trim();
			// If description ends with a numeric quantity and the quantity column is empty,
			// split the trailing number into the quantity column (index 2).
			// Require whitespace before the digits so that codes like "GL-01" or "Type-01"
			// are NOT mistakenly split (the digits in those cases follow a hyphen, not a space).
			boolean hasEmptyQty = (row.size() < 3 || row.get(2) == null || row.get(2).isBlank());
			java.util.regex.Matcher qtyMatcher = java.util.regex.Pattern.compile("(.*\\s)(\\d+(?:\\.\\d+)?)$").matcher(desc);
			if (hasEmptyQty && qtyMatcher.matches()) {
				String newDesc = qtyMatcher.group(1).trim();
				String qty = qtyMatcher.group(2);
				row.set(1, newDesc);
				// Ensure the row has at least three columns for the quantity slot.
				while (row.size() < 3) {
					row.add("");
				}
				row.set(2, qty);
				// Re-assign desc for the regex that follows
				desc = newDesc;
			}

			List<String[]> matches = new ArrayList<>();
			java.util.regex.Matcher m = java.util.regex.Pattern.compile(
					"(?:^|\\s+)([A-Za-z]|\\d+(?:\\.\\d+)*)\\s+([A-Za-z][^A-Z\\n]*?)(?=(?:\\s+[A-Za-z]\\s+[A-Za-z])|$)").matcher(desc);
			int firstMatchStart = -1;
			while (m.find()) {
				String code = m.group(1).trim();
				String itemText = m.group(2).trim();
				if (isValidExtractedCode(code, itemText, "", "")) {
					if (firstMatchStart == -1) {
						firstMatchStart = m.start();
					}
					matches.add(new String[]{code.toUpperCase(Locale.ROOT), itemText});
				}
			}

			if (matches.size() >= 2) {
				String preamble = (firstMatchStart > 0) ? desc.substring(0, firstMatchStart).trim() : "";
				if (!preamble.isBlank()) {
					List<String> preambleRow = new ArrayList<>(row);
					preambleRow.set(0, "");
					preambleRow.set(1, preamble);
					for (int c = 2; c < preambleRow.size(); c++) {
						preambleRow.set(c, "");
					}
					result.add(preambleRow);
				}

				String rawQty = row.size() > 2 && row.get(2) != null ? row.get(2).trim() : "";
				String rawUom = row.size() > 3 && row.get(3) != null ? row.get(3).trim() : "";
				String rawRate = row.size() > 4 && row.get(4) != null ? row.get(4).trim() : "";
				String rawAmount = row.size() > 5 && row.get(5) != null ? row.get(5).trim() : "";

				String[] uomTokens = rawUom.split("\\s+");
				String[] qtyTokens = rawQty.split("\\s+");
				String[] rateTokens = rawRate.split("\\s+");
				String[] amountTokens = rawAmount.split("\\s+");

				for (int i = 0; i < matches.size(); i++) {
					String[] item = matches.get(i);
					List<String> subRow = new ArrayList<>(row);
					subRow.set(0, item[0]);
					subRow.set(1, item[1]);
					if (subRow.size() > 2) {
						subRow.set(2, (i < qtyTokens.length && !qtyTokens[i].isBlank()) ? qtyTokens[i] : (qtyTokens.length == 1 ? qtyTokens[0] : ""));
					}
					if (subRow.size() > 3) {
						subRow.set(3, (i < uomTokens.length && !uomTokens[i].isBlank()) ? uomTokens[i] : (uomTokens.length == 1 ? uomTokens[0] : ""));
					}
					if (subRow.size() > 4) {
						subRow.set(4, (i < rateTokens.length && !rateTokens[i].isBlank()) ? rateTokens[i] : (rateTokens.length == 1 ? rateTokens[0] : ""));
					}
					if (subRow.size() > 5) {
						subRow.set(5, (i < amountTokens.length && !amountTokens[i].isBlank()) ? amountTokens[i] : (amountTokens.length == 1 ? amountTokens[0] : ""));
					}
					result.add(subRow);
				}
			} else {
				result.add(row);
			}
		}
		return result;
	}



	public List<BOQ> mapBOQData(Map<String, String> columnMapping, InputStream inputStream, int startPage)
			throws IOException, DataNotFoundException {
		List<BOQ> boqList = new ArrayList<>();
		PDDocument document = PDDocument.load(inputStream);
		try {
			hybridPdfExtractionEngine.validatePdfDocument(document);
			ObjectExtractor oe = new ObjectExtractor(document);
			PageIterator iterator = oe.extract();
			List<String> headers = null;
			int currentPageNumber = 0;
			while (iterator.hasNext()) {
				Page page = iterator.next();
				currentPageNumber++;
				if (currentPageNumber < startPage) continue;

				List<List<String>> rows = new ArrayList<>();
				try {
					rows = hybridPdfExtractionEngine.extractTableRows(document, page, currentPageNumber);
				} catch (Exception e) {
					continue;
				}
				rows = rows.stream().map(r -> r.stream().map(c -> c.replaceAll("[\\r\\n]+", " ").trim()).toList()).toList();
				rows = expandMergedRows(rows);
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
						String normalizedText = joinedRow.replaceAll("['’`]", "");
						if (joinedRow.contains("total") || joinedRow.contains("grand total")
								|| joinedRow.contains("carried to collection")) continue;
						BOQ boq = new BOQ();
						boolean hasValidData = false;
						Double parsedQuantity = null;
						String mappedUom = null;
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
										parsedQuantity = parsedQty;
										hasValidData = true;
									}
								}
								case "uom" -> {
									if (!value.isBlank()) {
//	                                boq.setUom(excelFileService.findUOM(value));
										mappedUom = value;
										hasValidData = true;
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
						boq.setLastLevel(BoqExtractionUtils.isLastLevel(parsedQuantity, mappedUom));
						boolean isOrphanRevisionNumber = (boq.getBoqCode() == null || boq.getBoqCode().isBlank())
								&& boq.getQuantity() == 0.0
								&& (boq.getUomCode() == null || boq.getUomCode().isBlank() || boq.getUomCode().equals("-"))
								&& boq.getBoqName() != null && boq.getBoqName().trim().matches("^\\d{1,2}$");
						if (hasValidData && !isOrphanRevisionNumber) boqList.add(boq);
					}
			}
		} finally {
			document.close();
		}
		return boqList;
	}

	private Boolean parseBooleanFlag(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		String normalized = value.trim().toLowerCase(Locale.ROOT);
		if (normalized.equals("-")) {
			return null;
		}
		return !Set.of("false", "no", "n", "0", "0.0").contains(normalized);
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
					if (b1.getExcelSno() != null && b2.getExcelSno() != null) {
						int excelSnoOrder = b1.getExcelSno().compareTo(b2.getExcelSno());
						if (excelSnoOrder != 0) return excelSnoOrder;
					} else if (b1.getExcelSno() == null && b2.getExcelSno() != null) {
						return 1;
					} else if (b1.getExcelSno() != null) {
						return -1;
					}
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
												  Integer requestedEndPage,
												  String pageBreakWord) throws IOException {

		List<ExcelExtractBOQ> boqList = new ArrayList<>();
		Map<String, List<Integer>> mappedColumnIndexes = new HashMap<>();
		List<String> normalizedHeaders = null;

		try (InputStream inputStream = boqFile.getInputStream();
			 PDDocument document = PDDocument.load(inputStream)) {

			hybridPdfExtractionEngine.validatePdfDocument(document);
			int endPage = validatePageRange(document, startPage, requestedEndPage);

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
				if (currentPageNumber > endPage) {
					break;
				}

				boolean pageBreakFound = containsPageBreakWord(document, currentPageNumber, pageBreakWord);
				List<List<String>> rows = new ArrayList<>();
				try {
					rows = hybridPdfExtractionEngine.extractTableRows(document, page, currentPageNumber);
				} catch (Exception e) {
					throw new IOException("Failed to extract BOQ data from PDF page "
							+ currentPageNumber + ".", e);
				}
				rows = rowsFromBoqHeader(rows);
				rows = expandMergedRows(rows);

					if (rows.isEmpty()) {
						if (pageBreakFound) {
							currentBoqPageNo++;
						}
						continue;
					}

					boolean isPageHeader = (normalizedHeaders == null) || isHeaderMatch(rows.get(0), normalizedHeaders);
					int dataStartIndex = 0;

					if (isPageHeader) {
						normalizedHeaders = rows.get(0).stream()
								.map(h -> h == null ? "" : h.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", ""))
								.toList();
						mappedColumnIndexes.clear();

						for (Map.Entry<String, List<String>> mapping : columnMapping.entrySet()) {

							String internalKey = mapping.getKey();
							List<String> expectedHeaders = mapping.getValue();
							if (expectedHeaders == null) continue;

							for (String expectedHeader : expectedHeaders) {
								if (expectedHeader == null) continue;
								String normalizedExpected = expectedHeader
										.toLowerCase(Locale.ROOT)
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

						for (Map.Entry<String, List<String>> mapping : columnMapping.entrySet()) {
							String internalKey = mapping.getKey();
							List<Integer> indices = mappedColumnIndexes.get(internalKey);
							if (indices == null || indices.isEmpty()) {
								for (int i = 0; i < normalizedHeaders.size(); i++) {
									String actualHeader = normalizedHeaders.get(i);
									if (matchesSemanticField(internalKey, actualHeader)) {
										mappedColumnIndexes.computeIfAbsent(internalKey, k -> new ArrayList<>()).add(i);
										break;
									}
								}
							}
						}
						dataStartIndex = 1;
					}

					for (int i = dataStartIndex; i < rows.size(); i++) {

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

						boolean isHeaderRow = (normalizedHeaders != null && !normalizedHeaders.isEmpty() && (
								normalizedHeaders.stream()
										.filter(h -> !h.isBlank())
										.allMatch(normalizedRow::contains)
								|| isHeaderMatch(row, normalizedHeaders)));

						if (isHeaderRow) {
							continue;
						}

						String joinedRow = String.join(" ", row).toLowerCase();
						String normalizedText = joinedRow.replaceAll("['’`]", "");

						if (joinedRow.replaceAll("[.\\-_\\s…]+", "").isEmpty()) {
							continue;
						}

						if (joinedRow.matches(".*page\\s+[ivxlcdm\\-]+.*")) {
							continue;
						}

						if (joinedRow.contains("carried to summary")
								|| joinedRow.contains("carried to grand summary")
								|| joinedRow.contains("carried to collection")
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

						ExcelExtractBOQ boq = new ExcelExtractBOQ();

						boq.setSno(serialNumber);
						boq.setPageNo(currentBoqPageNo);

						boolean hasValidData = false;
						String uomFromQuantity = null;

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

								case "rateOnly" -> {
									Boolean value = parseBooleanFlag(values.get(0));
									if (value != null) {
										boq.setRateOnly(value);
										hasValidData = true;
									}
								}

								case "provisionalSum" -> {
									Boolean value = parseBooleanFlag(values.get(0));
									if (value != null) {
										boq.setProvisionalSum(value);
										hasValidData = true;
									}
								}

                                 case "notQuotedBoq" -> {
									Boolean value = parseBooleanFlag(values.get(0));
									if (value != null) {
										boq.setNotQuotedBoq(value);
										hasValidData = true;
									}
								}
 
								case "quantity" -> {

									for (String val : values) {

										Double parsedQty = parseQuantity(val);

										if (parsedQty != null) {
											boq.setQuantity(parsedQty);
											uomFromQuantity = extractTrailingUom(val);
											hasValidData = true;
											break;
										}
									}
									
								}

								case "uom" -> {

									String uomValue = values.get(0);

									if (!uomValue.isBlank()) {
										java.util.regex.Matcher uomQtyMatcher = java.util.regex.Pattern.compile(
												"(?i)^([a-z0-9²³/%]+)\\s+(\\d+(?:[,.]\\d+)?)$").matcher(uomValue.trim());
										if (uomQtyMatcher.matches()) {
											boq.setUom(uomQtyMatcher.group(1));
											if (boq.getQuantity() == null) {
												try {
													boq.setQuantity(Double.parseDouble(uomQtyMatcher.group(2).replace(",", "")));
												} catch (Exception ignored) {}
											}
										} else {
											String extractedUom = extractTrailingUom(uomValue);
											boq.setUom(extractedUom != null ? extractedUom : uomValue);
										}
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

						if ((boq.getUom() == null || boq.getUom().isBlank())
								&& uomFromQuantity != null) {
							boq.setUom(uomFromQuantity);
						}
						normalizePdfItemCodeAndDescription(boq);

						if ((boq.getBoqCode() == null || boq.getBoqCode().isBlank()) && boq.getBoqName() != null) {
							java.util.regex.Matcher m = CODE_IN_DESC.matcher(boq.getBoqName());
							if (m.matches()) {
								String possibleCode = m.group(1);
								String restDesc = m.group(2).trim();
								if (isValidExtractedCode(possibleCode, restDesc, boq.getUom(),
										boq.getQuantity() != null ? String.valueOf(boq.getQuantity()) : "")) {
									boq.setBoqCode(possibleCode.toUpperCase(Locale.ROOT));
									boq.setBoqName(restDesc);
								}
							} else if (boq.getUom() != null && !boq.getUom().isBlank() && !boq.getUom().equals("-")) {
								// Fallback: ONLY when the row has valid UOM data, check for isolated letter code embedded between description words (e.g. "... (temporary/ permanent) E during the construction process...")
								java.util.regex.Matcher midMatcher = java.util.regex.Pattern.compile(
										"(?s)^(.*?)\\s+([B-Zb-z])\\s+([a-z].*)$").matcher(boq.getBoqName());
								if (midMatcher.matches()) {
									String prefix = midMatcher.group(1).trim();
									String possibleCode = midMatcher.group(2).trim();
									String suffix = midMatcher.group(3).trim();
									boq.setBoqCode(possibleCode.toUpperCase(Locale.ROOT));
									boq.setBoqName(prefix + " " + suffix);
								}
							}
						}

						boq.setPageNo(resolveBoqPageNumber(
								currentPageNumber, currentBoqPageNo, pageBreakWord));
						boq.setLastLevel(BoqExtractionUtils.isLastLevel(
								boq.getQuantity(), boq.getUom()));

						boolean isOrphanRevisionNumber = (boq.getBoqCode() == null || boq.getBoqCode().isBlank())
								&& boq.getQuantity() == null
								&& (boq.getUom() == null || boq.getUom().isBlank() || boq.getUom().equals("-"))
								&& boq.getBoqName() != null && boq.getBoqName().trim().matches("^\\d{1,2}$");

						if (hasValidData && !isOrphanRevisionNumber) {
							boqList.add(boq);
							serialNumber++;
						}

					}
				if (pageBreakFound) {
					currentBoqPageNo++;
				}
			}
		}

		return boqList;
	}

	static void normalizePdfItemCodeAndDescription(ExcelExtractBOQ boq) {
		String rawCode = boq.getBoqCode();
		if (rawCode == null || rawCode.isBlank()) {
			return;
		}

		String description = boq.getBoqName();
		String quantity = boq.getQuantity() == null ? "" : String.valueOf(boq.getQuantity());
		java.util.regex.Matcher footnoteCode = java.util.regex.Pattern
				.compile("^1\\s+([A-Za-z])\\s+(.+)$")
				.matcher(rawCode.trim());
		if (footnoteCode.matches()
				&& isValidExtractedCode(footnoteCode.group(1), footnoteCode.group(2), boq.getUom(), quantity)) {
			boq.setBoqCode(footnoteCode.group(1).toUpperCase(Locale.ROOT));
			boq.setBoqName(joinDescription(footnoteCode.group(2), description));
			return;
		}

		java.util.regex.Matcher codeAndDescription = CODE_IN_DESC.matcher(rawCode.trim());
		if (codeAndDescription.matches()
				&& isValidExtractedCode(codeAndDescription.group(1), codeAndDescription.group(2),
						boq.getUom(), quantity)) {
			boq.setBoqCode(codeAndDescription.group(1).toUpperCase(Locale.ROOT));
			boq.setBoqName(joinDescription(codeAndDescription.group(2), description));
			return;
		}

		boolean standaloneCode = isValidExtractedCode(rawCode.trim(), description, boq.getUom(), quantity);
		if (!standaloneCode) {
			boq.setBoqCode(null);
			boq.setBoqName(joinDescription(rawCode, description));
		}
	}

	private static String joinDescription(String first, String second) {
		if (first == null || first.isBlank()) return second;
		if (second == null || second.isBlank()) return first.trim();
		return first.trim() + " " + second.trim();
	}

	static String extractTrailingUom(String value) {
		if (value == null) return null;
		java.util.regex.Matcher matcher = java.util.regex.Pattern
				.compile("^\\s*[-+]?(?:\\d[\\d,]*(?:\\.\\d+)?|\\.\\d+)\\s+([\\p{L}\\p{N}\\p{M}./^_%+-]+)\\s*$")
				.matcher(value);
		if (!matcher.matches()) return null;

		String uom = matcher.group(1).trim();
		return uom.isEmpty() || uom.matches("[-–—]+") ? null : uom;
	}

	static boolean containsPageBreakWord(PDDocument document, int pageNumber, String pageBreakWord)
			throws IOException {
		if (pageBreakWord == null || pageBreakWord.isBlank()) {
			return false;
		}
		PDFTextStripper stripper = new PDFTextStripper();
		stripper.setStartPage(pageNumber);
		stripper.setEndPage(pageNumber);
		String pageText = stripper.getText(document);
		String normalizedMarker = normalizePageBreakText(pageBreakWord);
		return !normalizedMarker.isEmpty()
				&& normalizePageBreakText(pageText).contains(normalizedMarker);
	}

	private static String normalizePageBreakText(String text) {
		return text == null ? "" : text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
	}

	static long resolveBoqPageNumber(int pdfPageNumber, long currentBoqPageNumber, String pageBreakWord) {
		return pageBreakWord == null || pageBreakWord.isBlank()
				? pdfPageNumber
				: currentBoqPageNumber;
	}

	int validatePageRange(PDDocument document, int startPage, Integer requestedEndPage) throws IOException {
		int pageCount = document.getNumberOfPages();
		int endPage = requestedEndPage == null ? startPage : requestedEndPage;
		if (startPage < 1 || startPage > pageCount || endPage < startPage || endPage > pageCount) {
			throw new IOException("Invalid PDF page range. Select pages from 1 to " + pageCount
					+ " with the end page greater than or equal to the start page.");
		}
		return endPage;
	}
}