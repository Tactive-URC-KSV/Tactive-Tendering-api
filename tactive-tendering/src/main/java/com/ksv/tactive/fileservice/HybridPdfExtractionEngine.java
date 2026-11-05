package com.ksv.tactive.fileservice;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Service;
import technology.tabula.Page;
import technology.tabula.Table;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.awt.Rectangle;

@Service
public class HybridPdfExtractionEngine {

    private final JavaOcrTableService javaOcrTableService;
    private final FormattedBoqPdfExtractor formattedBoqPdfExtractor;

    public HybridPdfExtractionEngine(
            JavaOcrTableService javaOcrTableService,
            FormattedBoqPdfExtractor formattedBoqPdfExtractor) {
        this.javaOcrTableService = javaOcrTableService;
        this.formattedBoqPdfExtractor = formattedBoqPdfExtractor;
    }

    /**
     * Validates that the PDF can be read and is not password protected.
     */
    public void validatePdfDocument(PDDocument document) {
        if (document.isEncrypted()) {
            throw new IllegalArgumentException("Password-protected PDFs are not supported.");
        }
        
        if (document.getNumberOfPages() == 0) {
            throw new IllegalArgumentException("The PDF contains no pages.");
        }
    }

    /**
     * Extracts table rows from a PDF page using a hybrid approach.
     * Detects and rejects fully scanned pages.
     * Uses Tabula for selectable text and Java OCR for scanned or poorly structured tables.
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
            List<List<String>> formattedRows = formattedBoqPdfExtractor.extractRows(document, pageIndex);
            if (formattedRows.size() > 1) {
                return formattedRows;
            }

            List<JavaOcrTableService.OcrWord> positionedWords =
                    extractPositionedWords(document, pageIndex);
            List<List<String>> positionedRows = javaOcrTableService.toBoqRows(
                    positionedWords, (int) Math.round(page.getWidth()), (int) Math.round(page.getHeight()));
            if (positionedRows.size() > 1) {
                return positionedRows;
            }

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

        if (rows.isEmpty() || !hasMinimumColumns(rows, 3)) {
            PDFRenderer renderer = new PDFRenderer(document);
            var image = renderer.renderImageWithDPI(pageIndex - 1, 300);
            List<List<List<String>>> ocrTables = javaOcrTableService.extractTableFromImage(image);
            List<List<String>> ocrRows = new ArrayList<>();
            for (List<List<String>> ocrTable : ocrTables) {
                ocrRows.addAll(ocrTable);
            }
            if (!ocrRows.isEmpty()) {
                rows = ocrRows;
            }
        }

        return rows;
    }

    private List<JavaOcrTableService.OcrWord> extractPositionedWords(
            PDDocument document, int pageIndex) throws Exception {
        List<JavaOcrTableService.OcrWord> words = new ArrayList<>();
        PDFTextStripper positionedStripper = new PDFTextStripper() {
            @Override
            protected void writeString(String text, List<TextPosition> textPositions) {
                StringBuilder wordText = new StringBuilder();
                double left = 0;
                double top = 0;
                double right = 0;
                double bottom = 0;
                for (TextPosition position : textPositions) {
                    String character = position.getUnicode();
                    if (character == null || character.isBlank()) {
                        addPositionedWord(words, wordText, left, top, right, bottom);
                        wordText.setLength(0);
                        continue;
                    }

                    double x = position.getXDirAdj();
                    double y = position.getYDirAdj();
                    double characterRight = x + position.getWidthDirAdj();
                    double characterBottom = y + position.getHeightDir();
                    if (wordText.isEmpty()) {
                        left = x;
                        top = y;
                        right = characterRight;
                        bottom = characterBottom;
                    } else {
                        left = Math.min(left, x);
                        top = Math.min(top, y);
                        right = Math.max(right, characterRight);
                        bottom = Math.max(bottom, characterBottom);
                    }
                    wordText.append(character);
                }
                addPositionedWord(words, wordText, left, top, right, bottom);
            }
        };
        positionedStripper.setStartPage(pageIndex);
        positionedStripper.setEndPage(pageIndex);
        positionedStripper.getText(document);
        return words;
    }

    private void addPositionedWord(
            List<JavaOcrTableService.OcrWord> words,
            StringBuilder wordText,
            double left,
            double top,
            double right,
            double bottom) {
        if (wordText.isEmpty()) {
            return;
        }
        int x = (int) Math.floor(left);
        int y = (int) Math.floor(top);
        int width = Math.max(1, (int) Math.ceil(right) - x);
        int height = Math.max(1, (int) Math.ceil(bottom) - y);
        words.add(new JavaOcrTableService.OcrWord(
                wordText.toString(), new Rectangle(x, y, width, height)));
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
                rows.add(normalizeTabulaRow(rawCells));
            }
        }
        return rows;
    }

    static List<String> normalizeTabulaRow(List<String> cells) {
        return cells.stream()
                .map(cell -> cell == null ? "" : cell.replaceAll("\\s*\\R\\s*", " ").trim())
                .toList();
    }
}
