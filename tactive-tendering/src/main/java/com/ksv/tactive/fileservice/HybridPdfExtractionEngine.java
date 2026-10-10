package com.ksv.tactive.fileservice;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import technology.tabula.Page;
import technology.tabula.Table;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class HybridPdfExtractionEngine {

    @Autowired
    private OcrClientService ocrClientService;

    /**
     * Validates the entire PDF document to ensure it is not fully scanned/image-based.
     * Throws an exception if the document does not contain sufficient meaningful native text.
     */
    public void validatePdfDocument(PDDocument document) {
        if (document.isEncrypted()) {
            throw new IllegalArgumentException("Password-protected PDFs are not supported.");
        }
        
        try {
            PDFTextStripper stripper = new PDFTextStripper();
            boolean hasNativeTextPage = false;
            
            for (int i = 1; i <= document.getNumberOfPages(); i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageText = stripper.getText(document);
                
                if (pageText != null && pageText.trim().length() > 200) {
                    hasNativeTextPage = true;
                    break;
                }
            }
            
            if (!hasNativeTextPage) {
                throw new IllegalArgumentException("Fully scanned or image-based PDFs are not supported. Please upload a PDF containing selectable text.");
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) throw (IllegalArgumentException)e;
            throw new IllegalArgumentException("Failed to read PDF document text for validation.", e);
        }
    }

    /**
     * Extracts table rows from a PDF page using a hybrid approach.
     * Detects and rejects fully scanned pages.
     * Uses Tabula if possible, and falls back to PaddleOCR for complex borderless tables.
     * 
     * @param document The PDDocument.
     * @param page Tabula's Page object.
     * @param pageIndex 1-based index of the page in the document.
     * @return List of rows (each row is a List of Strings representing cells).
     */
    public List<List<String>> extractTableRows(PDDocument document, Page page, int pageIndex) throws Exception {
        
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(pageIndex);
        stripper.setEndPage(pageIndex);
        String pageText = stripper.getText(document);
        
        List<List<String>> rows = new ArrayList<>();

        // If the page has sufficient text, try Tabula
        if (pageText != null && pageText.trim().length() >= 50) {
            // Try Tabula's SpreadsheetExtractionAlgorithm (for bordered tables)
            SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
            List<Table> tables = sea.extract(page);
            
            if (hasValidTables(tables)) {
                rows = extractRowsFromTabula(tables);
            } else {
                // Fallback to Tabula's BasicExtractionAlgorithm (for borderless tables)
                BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();
                List<Table> basicTables = bea.extract(page);
                
                if (hasValidTables(basicTables)) {
                    rows = extractRowsFromTabula(basicTables);
                }
            }
        }

        // 4. If Tabula didn't find anything useful, or we want PaddleOCR to reconstruct layout robustly
        if (rows.isEmpty() || !hasMinimumColumns(rows, 3)) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(pageIndex - 1, 300); // 300 DPI for OCR
            
            try {
                List<List<List<String>>> ocrTables = ocrClientService.extractTableFromImage(image);
                for (List<List<String>> ocrTable : ocrTables) {
                    rows.addAll(ocrTable);
                }
            } catch (Exception e) {
                System.err.println("PaddleOCR failed or is unavailable for page " + pageIndex + ", returning what we have.");
            }
        }

        return rows;
    }

    private boolean hasValidTables(List<Table> tables) {
        if (tables == null || tables.isEmpty()) {
            return false;
        }
        for (Table table : tables) {
            if (table.getRowCount() > 1 && table.getColCount() >= 2) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMinimumColumns(List<List<String>> rows, int minColumns) {
        for (List<String> row : rows) {
            if (row.size() >= minColumns) {
                return true;
            }
        }
        return false;
    }

    private List<List<String>> extractRowsFromTabula(List<Table> tables) {
        List<List<String>> rows = new ArrayList<>();
        for (Table table : tables) {
            for (var tableRow : table.getRows()) {
                List<String> rawCells = tableRow.stream()
                        .map(cell -> cell.getText() != null ? cell.getText().trim() : "")
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
        }
        return rows;
    }
}
