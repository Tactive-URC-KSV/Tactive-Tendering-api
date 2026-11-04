package com.ksv.tactive.fileservice;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class JavaOcrTableService {

    private static final List<String> BOQ_HEADERS =
            List.of("S.R. NO.", "ITEM DESCRIPTION", "QTY.", "UNIT", "RATE", "AMOUNT");
    private static final Pattern SERIAL_TOKEN = Pattern.compile("^(?:sr|no)$");

    private final String configuredTessdataPath;
    private final String language;
    private volatile Path bundledTessdataPath;

    public JavaOcrTableService(
            @Value("${ocr.tessdata.path:}") String tessdataPath,
            @Value("${ocr.tessdata.language:eng}") String language) {
        this.configuredTessdataPath = tessdataPath;
        this.language = language;
    }

    public List<List<List<String>>> extractTableFromImage(BufferedImage image) throws Exception {
        Path tessdataPath = resolveTessdataPath();
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tessdataPath.toString());
        tesseract.setLanguage(language);
        List<Word> recognizedWords = tesseract.getWords(image, ITessAPI.TessPageIteratorLevel.RIL_WORD);

        List<OcrWord> words = recognizedWords.stream()
                .filter(word -> word.getText() != null && !word.getText().isBlank())
                .map(word -> new OcrWord(word.getText().trim(), word.getBoundingBox()))
                .toList();
        List<List<String>> table = toBoqRows(words, image.getWidth(), image.getHeight());
        return table.size() > 1 ? List.of(table) : List.of();
    }

    private Path resolveTessdataPath() throws IOException {
        if (configuredTessdataPath != null && !configuredTessdataPath.isBlank()) {
            Path configuredPath = Path.of(configuredTessdataPath).toAbsolutePath().normalize();
            if (!Files.isRegularFile(configuredPath.resolve(language + ".traineddata"))) {
                throw new IllegalStateException("Tesseract language data was not found at "
                        + configuredPath.resolve(language + ".traineddata")
                        + ". Configure ocr.tessdata.path to a tessdata directory containing the selected language.");
            }
            return configuredPath;
        }

        Path cachedPath = bundledTessdataPath;
        if (cachedPath != null) {
            return cachedPath;
        }
        synchronized (this) {
            if (bundledTessdataPath != null) {
                return bundledTessdataPath;
            }
            String modelName = language + ".traineddata";
            try (InputStream model = JavaOcrTableService.class.getResourceAsStream("/tessdata/" + modelName)) {
                if (model == null) {
                    throw new IllegalStateException("Tesseract language data " + modelName
                            + " is not bundled. Configure ocr.tessdata.path to a tessdata directory containing it.");
                }
                Path tempPath = Files.createTempDirectory("tactive-tessdata-");
                tempPath.toFile().deleteOnExit();
                Path languageData = tempPath.resolve(modelName);
                Files.copy(model, languageData);
                languageData.toFile().deleteOnExit();
                bundledTessdataPath = tempPath;
                return tempPath;
            }
        }
    }

    List<List<String>> toBoqRows(List<OcrWord> words, int imageWidth, int imageHeight) {
        List<WordLine> lines = groupLines(words, imageHeight);
        WordLine headerLine = lines.stream()
                .filter(this::isBoqHeader)
                .findFirst()
                .orElse(null);
        if (headerLine == null) {
            return List.of();
        }

        List<OcrWord> headerWords = new ArrayList<>(headerLine.words);
        double headerBand = imageHeight * 0.04;
        for (WordLine line : lines) {
            String text = normalized(line.text());
            if (line != headerLine
                    && Math.abs(line.centerY() - headerLine.centerY()) <= headerBand
                    && isSecondaryHeader(text)) {
                headerWords.addAll(line.words);
            }
        }
        double[] boundaries = columnBoundaries(headerWords, imageWidth);
        List<List<String>> rows = new ArrayList<>();
        rows.add(BOQ_HEADERS);

        boolean headerAtTop = headerLine.centerY() < imageHeight / 2.0;
        WordLine previousDataLine = null;
        for (WordLine line : lines) {
            String normalizedLine = normalized(line.text());
            boolean secondaryHeader = line != headerLine
                    && Math.abs(line.centerY() - headerLine.centerY()) <= headerBand
                    && isSecondaryHeader(normalizedLine);
            if (line == headerLine || (headerAtTop && line.centerY() <= headerLine.centerY())
                    || (!headerAtTop && line.centerY() >= headerLine.centerY())
                    || secondaryHeader
                    || isPageHeaderOrFooter(line)) {
                continue;
            }

            List<String> row = emptyCells();
            for (OcrWord word : line.words) {
                if (word.text.matches("[-\u2013\u2014]+")) {
                    continue;
                }
                int column = columnFor(word.centerX(), boundaries);
                row.set(column, append(row.get(column), word.text));
            }

            if (row.stream().allMatch(String::isBlank)) {
                continue;
            }
            boolean alignedSerialContinuation = !row.get(1).isBlank()
                    && !row.get(0).isBlank()
                    && rows.size() > 1
                    && rows.get(rows.size() - 1).get(0).isBlank()
                    && startsAtSameDescriptionColumn(previousDataLine, line, boundaries, imageWidth);
            if (isDescriptionContinuation(row, rows, previousDataLine, line, boundaries, imageWidth)
                    && isCloseWrappedLine(previousDataLine, line, alignedSerialContinuation)) {
                int previousIndex = rows.size() - 1;
                List<String> previous = new ArrayList<>(rows.get(previousIndex));
                if (previous.get(0).isBlank()) {
                    previous.set(0, row.get(0));
                }
                boolean sameSerial = !row.get(0).isBlank()
                        && row.get(0).equals(previous.get(0));
                for (int column = 1; column < row.size(); column++) {
                    if (!row.get(column).isBlank()) {
                        if (previous.get(column).isBlank()) {
                            previous.set(column, row.get(column));
                        } else if (!previous.get(column).equals(row.get(column))) {
                            if (!sameSerial || column == 1) {
                                previous.set(column, append(previous.get(column), row.get(column)));
                            }
                        }
                    }
                }
                rows.set(previousIndex, previous);
            } else {
                rows.add(row);
            }
            previousDataLine = line;
        }
        return rows;
    }

    private boolean isCloseWrappedLine(
            WordLine previousLine, WordLine currentLine, boolean alignedSerialContinuation) {
        if (previousLine == null) {
            return false;
        }
        double lineHeight = Math.max(previousLine.height(), currentLine.height());
        double spacingMultiplier = alignedSerialContinuation ? 4.0 : 3.0;
        return currentLine.centerY() - previousLine.centerY()
                < Math.max(5.0, lineHeight * spacingMultiplier);
    }

    private List<WordLine> groupLines(List<OcrWord> words, int imageHeight) {
        List<OcrWord> ordered = words.stream()
                .sorted(Comparator.comparingDouble(OcrWord::centerY).thenComparingDouble(OcrWord::centerX))
                .toList();
        List<WordLine> lines = new ArrayList<>();
        for (OcrWord word : ordered) {
            WordLine line = lines.stream()
                    .filter(candidate -> Math.abs(candidate.centerY() - word.centerY())
                            <= Math.max(5.0, Math.min(word.box.height * 1.6, imageHeight * 0.015)))
                    .min(Comparator.comparingDouble(candidate ->
                            Math.abs(candidate.centerY() - word.centerY())))
                    .orElse(null);
            if (line == null) {
                line = new WordLine();
                lines.add(line);
            }
            line.add(word);
        }
        lines.sort(Comparator.comparingDouble(WordLine::centerY));
        return lines;
    }

    private boolean isBoqHeader(WordLine line) {
        String text = normalized(line.text());
        return text.contains("description")
                && (text.contains("qty") || text.contains("quantity"))
                && text.contains("unit");
    }

    private boolean isSecondaryHeader(String normalizedText) {
        return normalizedText.contains("rate")
                || normalizedText.contains("amount")
                || (normalizedText.contains("no") && normalizedText.contains("dirhams"))
                || normalizedText.equals("no")
                || normalizedText.equals("sr");
    }

    private double[] columnBoundaries(List<OcrWord> headerWords, int imageWidth) {
        double[] fallbackCenters = {
                imageWidth * 0.08, imageWidth * 0.37, imageWidth * 0.67,
                imageWidth * 0.77, imageWidth * 0.87, imageWidth * 0.96
        };
        double[] centers = fallbackCenters.clone();
        double descriptionCenter = anchor(headerWords, "description", -1);
        double quantityCenter = anchor(headerWords, "qty", anchor(headerWords, "quantity", -1));
        double unitCenter = anchor(headerWords, "unit", -1);
        double rateCenter = anchor(headerWords, "rate", -1);
        double amountCenter = anchor(headerWords, "amount", -1);
        double serialCenter = serialAnchor(headerWords, descriptionCenter);

        double[] detected = {
                serialCenter, descriptionCenter, quantityCenter, unitCenter, rateCenter, amountCenter
        };
        for (int i = 0; i < detected.length; i++) {
            if (detected[i] >= 0 && detected[i] < imageWidth) {
                centers[i] = detected[i];
            }
        }

        for (int i = 1; i < centers.length; i++) {
            if (centers[i] <= centers[i - 1]) {
                centers = fallbackCenters;
                break;
            }
        }

        double[] boundaries = new double[centers.length - 1];
        for (int i = 0; i < boundaries.length; i++) {
            boundaries[i] = (centers[i] + centers[i + 1]) / 2.0;
        }
        double serialRightEdge = headerWords.stream()
                .filter(word -> SERIAL_TOKEN.matcher(normalized(word.text)).matches())
                .mapToDouble(word -> word.box.getMaxX())
                .max()
                .orElse(-1);
        if (serialRightEdge >= 0) {
            boundaries[0] = Math.min(imageWidth - 1,
                    serialRightEdge + Math.max(2, imageWidth * 0.01));
        }
        double quantityLeftEdge = headerWords.stream()
                .filter(word -> {
                    String text = normalized(word.text);
                    return text.equals("qty") || text.equals("quantity");
                })
                .mapToDouble(word -> word.box.getMinX())
                .findFirst()
                .orElse(-1);
        if (quantityLeftEdge >= 0) {
            boundaries[1] = Math.max(boundaries[0] + 1,
                    quantityLeftEdge - Math.max(2, imageWidth * 0.01));
        }
        return boundaries;
    }

    private double anchor(List<OcrWord> headerWords, String token, double fallback) {
        return headerWords.stream()
                .filter(word -> normalized(word.text).contains(token))
                .mapToDouble(OcrWord::centerX)
                .findFirst()
                .orElse(fallback);
    }

    private double serialAnchor(List<OcrWord> headerWords, double descriptionCenter) {
        List<OcrWord> serialWords = headerWords.stream()
                .filter(word -> SERIAL_TOKEN.matcher(normalized(word.text)).matches())
                .filter(word -> descriptionCenter < 0 || word.centerX() < descriptionCenter)
                .toList();
        return serialWords.stream().mapToDouble(OcrWord::centerX).average().orElse(-1);
    }

    private int columnFor(double centerX, double[] boundaries) {
        for (int i = 0; i < boundaries.length; i++) {
            if (centerX < boundaries[i]) {
                return i;
            }
        }
        return boundaries.length;
    }

    private boolean isPageHeaderOrFooter(WordLine line) {
        String text = normalized(line.text());
        return text.startsWith("project")
                || text.startsWith("location")
                || text.startsWith("client")
                || text.startsWith("billno")
                || text.contains("totalcarriedtocollection");
    }

    private boolean isDescriptionContinuation(
            List<String> row,
            List<List<String>> rows,
            WordLine previousLine,
            WordLine currentLine,
            double[] boundaries,
            int imageWidth) {
        if (rows.size() <= 1) {
            return false;
        }
        List<String> previous = rows.get(rows.size() - 1);
        if (previous.equals(BOQ_HEADERS)
                || previous.get(1).isBlank()) {
            return false;
        }
        if (row.get(1).isBlank()) {
            if (!row.get(0).isBlank()) {
                return false;
            }
            boolean hasContinuationCell = false;
            for (int column = 2; column < row.size(); column++) {
                if (!row.get(column).isBlank()) {
                    if (!previous.get(column).isBlank()
                            && !row.get(column).equals(previous.get(column))) {
                        return false;
                    }
                    hasContinuationCell = true;
                }
            }
            return hasContinuationCell;
        }
        if (!row.get(0).isBlank()) {
            if (!previous.get(0).isBlank()) {
                return row.get(0).equals(previous.get(0));
            }
            return startsAtSameDescriptionColumn(previousLine, currentLine, boundaries, imageWidth);
        }
        for (int column = 2; column < row.size(); column++) {
            if (!row.get(column).isBlank()
                    && !row.get(column).equals(previous.get(column))
                    && (column != 3 || !previous.get(column).isBlank())) {
                return false;
            }
        }
        return true;
    }

    private boolean startsAtSameDescriptionColumn(
            WordLine previousLine,
            WordLine currentLine,
            double[] boundaries,
            int imageWidth) {
        if (previousLine == null || currentLine == null) {
            return false;
        }
        double previousStart = descriptionStartX(previousLine, boundaries);
        double currentStart = descriptionStartX(currentLine, boundaries);
        return previousStart >= 0
                && currentStart >= 0
                && Math.abs(previousStart - currentStart) <= Math.max(3, imageWidth * 0.01);
    }

    private double descriptionStartX(WordLine line, double[] boundaries) {
        return line.words.stream()
                .filter(word -> columnFor(word.centerX(), boundaries) == 1)
                .mapToDouble(word -> word.box.getMinX())
                .min()
                .orElse(-1);
    }

    private List<String> emptyCells() {
        return new ArrayList<>(List.of("", "", "", "", "", ""));
    }

    private String append(String existing, String value) {
        return existing.isBlank() ? value : existing + " " + value;
    }

    private String normalized(String text) {
        return text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    record OcrWord(String text, Rectangle box) {
        double centerX() {
            return box.getCenterX();
        }

        double centerY() {
            return box.getCenterY();
        }
    }

    private static final class WordLine {
        private final List<OcrWord> words = new ArrayList<>();

        private void add(OcrWord word) {
            words.add(word);
        }

        private double height() {
            return words.stream().mapToDouble(word -> word.box.getHeight()).max().orElse(0);
        }

        private double centerY() {
            return words.stream().mapToDouble(OcrWord::centerY).average().orElse(0);
        }

        private String text() {
            return words.stream().sorted(Comparator.comparingDouble(OcrWord::centerX))
                    .map(OcrWord::text).reduce((left, right) -> left + " " + right).orElse("");
        }
    }
}
