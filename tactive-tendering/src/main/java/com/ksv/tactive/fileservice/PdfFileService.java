package com.ksv.tactive.fileservice;

import com.itextpdf.text.Font;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.ksv.tactive.project.entity.UOM;
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
		document.close();
		return oe.extract();
	}

	public List<String> getColumnsHeaders(MultipartFile boqFile) throws IOException {
		InputStream inputStream = boqFile.getInputStream();
		PageIterator iterator = getPages(inputStream);
		List<List<String>> allRows = new ArrayList<>();
		List<String> headers = null;
		while (iterator.hasNext()) {
			Page page = iterator.next();
			SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
			List<Table> tables = sea.extract(page);
			for (Table table : tables) {
				List<List<String>> rows = table.getRows().stream()
						.map(row -> row.stream().map(cell -> cell.getText()).toList()).toList();
				if (rows.isEmpty())
					continue;
				if (headers == null) {
					headers = rows.get(0);
				}
				int startIndex = 0;
				if (rows.get(0).equals(headers)) {
					startIndex = 1;
				}
				for (int i = startIndex; i < rows.size(); i++) {
					allRows.add(rows.get(i));
				}
			}
		}
		return headers;
	}
	public List<BOQ> mapBOQData(Map<String, String> columnMapping, InputStream inputStream)
	        throws IOException, DataNotFoundException {
	    List<BOQ> boqList = new ArrayList<>();
	    PageIterator iterator = getPages(inputStream);
	    List<String> headers = null;
	    while (iterator.hasNext()) {
	        Page page = iterator.next();
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
	                            if (parsedQty != null && parsedQty != 0) {
	                                boq.setQuantity(parsedQty);
	                                boq.setLastLevel(true);
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
	    return boqList;
	}
	private Double parseQuantity(String value) {
	    if (value == null) return null;
	    String cleaned = value.trim();
	    boolean isNegative = cleaned.startsWith("(") && cleaned.endsWith(")");
	    cleaned = cleaned.replaceAll("[()]", "");
	    cleaned = cleaned.replaceAll("[^0-9.,-]", "");
	    cleaned = cleaned.replaceAll(",", "");
	    if (cleaned.isEmpty()) return null;
	    try {
	        double result = Double.parseDouble(cleaned);
	        return isNegative ? -result : result;
	    } catch (NumberFormatException e) {
	        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("[-+]?[0-9]*\\.?[0-9]+").matcher(value);
	        if (matcher.find()) {
	            try {
	                return Double.parseDouble(matcher.group());
	            } catch (NumberFormatException ignored) {}
	        }
	    }
	    return null;
	}
	public void generateBoqPdf(List<BOQ> boqList, HttpServletResponse response) throws IOException, DocumentException {
		String projectName = boqList.get(0).getProject().getProjectName().trim().replaceAll("\\s+", "_");
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
		String[] headers = { "S No", "BOQ Code", "BOQ Name", "UOM", "Quantity", "Rate", "Amount", "Parent Code",
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
					safe(boq.getBoqName()), boq.getUom() != null ? getUomCode(boq.getUom().getId()) : "",
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
		UOM uom = masterService.findUomById(uomId);
		if (uom != null) {
			return uom.getUomCode();
		}
		return null;
	}
	private String safe(String val) {
		return val == null ? "" : val;
	}
	public List<ExcelExtractBOQ> getExtractedData(MultipartFile boqFile, Map<String, String> columnMapping,
	        int startRow, int endRow) throws IOException {
	    InputStream inputStream = boqFile.getInputStream();
	    PageIterator iterator = getPages(inputStream);
	    List<String> headers = null;
	    List<ExcelExtractBOQ> boqList = new ArrayList<>();
	    int globalRowCounter = 0;  
	    int serialNumber = 1;
	    while (iterator.hasNext()) {
	        Page page = iterator.next();
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
	                globalRowCounter++;
	                if (startRow > 0 && globalRowCounter < startRow) {
	                    continue;
	                }
	                if (endRow > 0 && globalRowCounter > endRow) {
	                    return boqList; 
	                }
	                if (row.stream().allMatch(String::isBlank)) continue;
	                String joinedRow = String.join(" ", row).toLowerCase();
	                if (joinedRow.contains("total") || joinedRow.contains("grand total")) continue;
	                ExcelExtractBOQ boq = new ExcelExtractBOQ();
	                boq.setSno(serialNumber++);
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
	                            if (parsedQty != null && parsedQty != 0) {
	                                boq.setQuantity(parsedQty);
	                                hasValidData = true;
	                            }
	                        }
	                        case "uom" -> {
	                            if (!value.isBlank()) {
	                                boq.setUom(value);
	                                hasValidData = true;
	                            }
	                        }
	                    }
	                }
	                boq.setLastLevel(boq.getQuantity() > 0 || (boq.getUom() != null && !boq.getUom().isBlank()));
	                if (hasValidData) boqList.add(boq);
	            }
	        }
	    }
	    return boqList;
	}
}
