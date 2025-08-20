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
import com.ksv.tactive.project.entity.BOQ;

import jakarta.servlet.http.HttpServletResponse;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

@Service
public class PdfFileService {

	@Autowired
	private ExcelFileService excelFileService;

	private PageIterator getPages(InputStream boqFile) throws IOException {
		PDDocument document = PDDocument.load(boqFile);
		ObjectExtractor oe = new ObjectExtractor(document);
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

	public List<BOQ> mapBOQData(Map<String, String> columnMapping, InputStream inputStream) throws IOException {
		List<BOQ> boqList = new ArrayList<>();

		PageIterator iterator = getPages(inputStream);
		List<String> headers = null;

		while (iterator.hasNext()) {
			Page page = iterator.next();
			SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
			List<Table> tables = sea.extract(page);

			for (Table table : tables) {
				List<List<String>> rows = table.getRows().stream()
						.map(row -> row.stream().map(cell -> cell.getText().trim()).toList()).toList();

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
					List<String> row = rows.get(i);
					BOQ boq = new BOQ();

					for (Map.Entry<String, String> entry : columnMapping.entrySet()) {
						String internalField = entry.getKey();
						String columnHeader = entry.getValue();

						int colIndex = headers.indexOf(columnHeader);
						if (colIndex == -1 || colIndex >= row.size())
							continue;

						String value = row.get(colIndex);

						switch (internalField) {
						case "slNo" -> {
							try {
								boq.setSlNo(Integer.valueOf(value));
							} catch (Exception ignored) {
							}
						}
						case "boqCode" -> boq.setBoqCode(value);
						case "boqName" -> boq.setBoqName(value);
						case "parentLevel" -> {
							try {
								boq.setParentLevel(Integer.valueOf(value));
							} catch (Exception ignored) {
							}
						}
						case "level" -> {
							try {
								boq.setLevel(Integer.valueOf(value));
							} catch (Exception ignored) {
							}
						}
						case "quantity" -> {
							try {
								boq.setQuantity(Double.valueOf(value));
							} catch (Exception ignored) {
							}
						}
						case "uom" -> {
							if (!value.isBlank()) {
								boq.setUom(excelFileService.findUOM(value));
							}
						}
						case "supplyRate" -> {
							try {
								boq.setSupplyRate(Double.valueOf(value));
							} catch (Exception ignored) {
							}
						}
						case "installationRate" -> {
							try {
								boq.setInstallationRate(Double.valueOf(value));
							} catch (Exception ignored) {
							}
						}
						case "totalRate" -> {
							try {
								boq.setTotalRate(Double.valueOf(value));
							} catch (Exception ignored) {
							}
						}
						case "supplyAmount" -> {
							try {
								boq.setSupplyAmount(Double.valueOf(value));
							} catch (Exception ignored) {
							}
						}
						case "installationAmount" -> {
							try {
								boq.setInstallationAmount(Double.valueOf(value));
							} catch (Exception ignored) {
							}
						}
						case "totalAmount" -> {
							try {
								boq.setTotalAmount(Double.valueOf(value));
							} catch (Exception ignored) {
							}
						}
						case "remarks" -> boq.setRemarks(value);
						case "lastLevel" -> boq.setLastLevel("1".equals(value));
						case "rateOnly" -> boq.setRateOnly("1".equals(value));
						case "qtyType" -> boq.setQtyType(value);
						case "wo" -> {
							try {
								boq.setWo(Integer.valueOf(value));
							} catch (Exception ignored) {
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

	public void generateBoqPdf(List<BOQ> boqList, HttpServletResponse response) throws IOException, DocumentException {
	    String projectName = boqList.get(0).getProject().getProjectName()
	            .trim()
	            .replaceAll("\\s+", "_");

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

	    // Table setup
	    String[] headers = {"S No", "ID", "BOQ Code", "BOQ Name", "UOM", "Quantity", "Rate", "Amount", "Parent Code", "Last Level"};
	    float[] colWidths = {1f, 1f, 2f, 4f, 1.5f, 2f, 2f, 3f, 2f, 2f}; 

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
	        String[] rowData = {
	                String.valueOf(slNo++),
	                String.valueOf(boq.getId()),
	                safe(boq.getBoqCode()),
	                safe(boq.getBoqName()),
	                boq.getUom() != null ? boq.getUom().getUomCode() : "",
	                String.valueOf(boq.getQuantity()),
	                String.valueOf(boq.getTotalRate()),
	                String.valueOf(boq.getTotalAmount()),
	                boq.getParentBOQ() != null ? boq.getParentBOQ().getBoqCode() : "",
	                boq.isLastLevel() ? "Yes" : "No"
	        };

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

	private String safe(String val) {
	    return val == null ? "" : val;
	}

}
