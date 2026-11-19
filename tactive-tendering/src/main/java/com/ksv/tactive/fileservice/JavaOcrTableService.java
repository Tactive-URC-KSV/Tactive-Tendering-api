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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class JavaOcrTableService {

    private static final Pattern SERIAL_TOKEN = Pattern.compile("^(?:sr|no)$");
    private static final Pattern CODE_IN_DESC = Pattern.compile(
            "(?i)^([a-z]{1,3}|\\d+(?:\\.\\d+)*)[.)\\s]+(.*)$");

    private static boolean isValidExtractedCode(
            String code, String restDesc, String uom, String qty, String rate) {
        if (code == null || code.isBlank() || restDesc == null || restDesc.isBlank()) {
            return false;
        }
        String cleanCode = code.trim();
        boolean hasData = (uom != null && !uom.isBlank() && !uom.matches("[-–—]+"))
                || (qty != null && !qty.isBlank() && !qty.matches("[-–—]+"))
                || (rate != null && !rate.isBlank() && !rate.matches("[-–—]+"));
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
            headerLine = lines.stream()
                    .filter(line -> line.centerY() < imageHeight * 0.45 && line.words.size() >= 3)
                    .findFirst()
                    .orElse(null);
        }
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
        rows.add(extractHeaderCells(headerWords, boundaries));

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
            if (row.get(0).isBlank() && !row.get(1).isBlank()) {
                java.util.regex.Matcher m = CODE_IN_DESC.matcher(row.get(1));
                if (m.matches()) {
                    String possibleCode = m.group(1);
                    String restDesc = m.group(2).trim();
                    if (isValidExtractedCode(possibleCode, restDesc, row.get(3), row.get(2), row.get(4))) {
                        row.set(0, possibleCode.toUpperCase(Locale.ROOT));
                        row.set(1, restDesc);
                    }
                } else if (!row.get(3).isBlank() || !row.get(2).isBlank() || !row.get(4).isBlank()) {
                    java.util.regex.Matcher endMatcher = Pattern.compile("^(.*?)\\s+([B-Zb-z])$").matcher(row.get(1));
                    if (endMatcher.matches()) {
                        row.set(0, endMatcher.group(2).toUpperCase(Locale.ROOT));
                        row.set(1, endMatcher.group(1).trim());
                    }
                }
            } else if (!row.get(0).isBlank() && !row.get(1).isBlank()) {
                if (!isValidExtractedCode(row.get(0), row.get(1), row.get(3), row.get(2), row.get(4))) {
                    row.set(1, row.get(0) + " " + row.get(1));
                    row.set(0, "");
                }
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
                // Before adding this row, check if it has a code and the previous row
                // was just orphan description text (no code, no data columns).
                // If so, merge the previous row's description as a prefix of this row.
                if (!row.get(0).isBlank() && rows.size() > 1) {
                    List<String> lastRow = rows.get(rows.size() - 1);
                    boolean lastHasNoCode = lastRow.get(0).isBlank();
                    boolean lastHasNoData = lastRow.subList(2, lastRow.size()).stream().allMatch(String::isBlank);
                    if (lastHasNoCode && lastHasNoData && !lastRow.get(1).isBlank()) {
                        String prefixDesc = lastRow.get(1).trim();
                        boolean isHeading = isHeadingTitle(prefixDesc);
                        if (!isHeading) {
                            row.set(1, prefixDesc + " " + row.get(1));
                            rows.remove(rows.size() - 1);
                        }
                    }
                }
                rows.add(row);
            }
            previousDataLine = line;
        }
        return rows;
    }

    private boolean isCloseWrappedLine(
            WordLine previousLine, WordLine currentLine, boolean alignedSerialContinuation) {
        if (previousLine == null || currentLine == null) {
            return false;
        }
        double lineHeight = Math.max(previousLine.height(), currentLine.height());
        double maxGap = alignedSerialContinuation
                ? Math.max(20.0, lineHeight * 2.5)
                : lineHeight * 1.8;
        return currentLine.centerY() - previousLine.centerY()
                < Math.max(6.0, maxGap);
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

    private static final List<String> OCR_HEADER_KEYWORDS = List.of(
            "description", "desc", "item", "particular", "scope", "work",
            "qty", "quantity", "unit", "uom", "rate", "price", "amount",
            "total", "cost", "code", "no", "sno", "srno"
    );

    private boolean isBoqHeader(WordLine line) {
        String text = normalized(line.text());
        int count = 0;
        for (String kw : OCR_HEADER_KEYWORDS) {
            if (text.contains(kw)) {
                count++;
            }
        }
        return count >= 2 || (count >= 1 && line.words.size() >= 3);
    }

    private boolean isSecondaryHeader(String normalizedText) {
        return normalizedText.contains("rate")
                || normalizedText.contains("amount")
                || normalizedText.contains("price")
                || normalizedText.contains("total")
                || normalizedText.contains("unit")
                || normalizedText.contains("qty")
                || (normalizedText.contains("no") && normalizedText.length() <= 12)
                || normalizedText.equals("no")
                || normalizedText.equals("sr");
    }

    private double[] columnBoundaries(List<OcrWord> headerWords, int imageWidth) {
        double[] fallbackCenters = {
                imageWidth * 0.08, imageWidth * 0.37, imageWidth * 0.67,
                imageWidth * 0.77, imageWidth * 0.87, imageWidth * 0.96
        };
        double descriptionCenter = anchor(headerWords, "description",
                anchor(headerWords, "particular", anchor(headerWords, "item", -1)));
        double quantityCenter = anchor(headerWords, "qty", anchor(headerWords, "quantity", -1));
        double unitCenter = anchor(headerWords, "unit", anchor(headerWords, "uom", -1));
        double rateCenter = anchor(headerWords, "rate", anchor(headerWords, "price", -1));
        double amountCenter = anchor(headerWords, "amount", anchor(headerWords, "total", -1));
        double serialCenter = serialAnchor(headerWords, descriptionCenter);

        double[] centers = {
                serialCenter, descriptionCenter, quantityCenter, unitCenter, rateCenter, amountCenter
        };
        for (int i = 0; i < centers.length; i++) {
            if (centers[i] < 0 || centers[i] >= imageWidth) {
                centers = fallbackCenters.clone();
                break;
            }
        }
        Arrays.sort(centers);

        double[] boundaries = new double[centers.length - 1];
        for (int i = 0; i < boundaries.length; i++) {
            boundaries[i] = (centers[i] + centers[i + 1]) / 2.0;
        }
        double serialRightEdge = headerWords.stream()
                .filter(word -> SERIAL_TOKEN.matcher(normalized(word.text)).matches())
                .mapToDouble(word -> word.box.getMaxX())
                .max()
                .orElse(-1);
        double descriptionLeftEdge = headerWords.stream()
                .filter(word -> {
                    String text = normalized(word.text);
                    return text.contains("description") || text.contains("particular") || text.contains("item");
                })
                .mapToDouble(word -> word.box.getMinX())
                .min()
                .orElse(-1);

        if (serialRightEdge >= 0 && descriptionLeftEdge >= 0 && descriptionLeftEdge > serialRightEdge) {
            boundaries[0] = (serialRightEdge + descriptionLeftEdge) / 2.0;
        } else if (descriptionLeftEdge >= 0) {
            boundaries[0] = Math.max(boundaries[0], descriptionLeftEdge - Math.max(5, imageWidth * 0.02));
        } else if (serialRightEdge >= 0) {
            boundaries[0] = Math.min(imageWidth - 1,
                    serialRightEdge + Math.max(15, imageWidth * 0.03));
        }
        double firstNonDescriptionLeftEdge = headerWords.stream()
                .filter(word -> {
                    String text = normalized(word.text);
                    return !text.contains("description")
                            && !text.contains("particular")
                            && !SERIAL_TOKEN.matcher(text).matches()
                            && (text.contains("qty") || text.contains("quantity")
                            || text.contains("unit") || text.contains("uom")
                            || text.contains("rate") || text.contains("price")
                            || text.contains("amount") || text.contains("total"));
                })
                .mapToDouble(word -> word.box.getMinX())
                .min()
                .orElse(-1);
        if (firstNonDescriptionLeftEdge >= 0) {
            boundaries[1] = Math.max(boundaries[0] + 1,
                    firstNonDescriptionLeftEdge - Math.max(2, imageWidth * 0.01));
        }
        return boundaries;
    }

    private List<String> extractHeaderCells(List<OcrWord> headerWords, double[] boundaries) {
        List<String> headers = emptyCells();
        for (OcrWord word : headerWords) {
            int column = columnFor(word.centerX(), boundaries);
            headers.set(column, append(headers.get(column), word.text));
        }
        return headers;
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

    private static boolean isHeadingTitle(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String trimmed = text.trim();
        String[] words = trimmed.split("\\s+");
        if (words.length > 5) {
            return false;
        }
        if (trimmed.endsWith(".") || trimmed.endsWith(":") || trimmed.endsWith(",")) {
            return false;
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.startsWith("the ") || lower.startsWith("allow ") || lower.startsWith("provide ")
                || lower.startsWith("to ") || lower.startsWith("for ")) {
            return false;
        }
        return true;
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
        if (previous.equals(rows.get(0))
                || previous.get(1).isBlank()) {
            return false;
        }

        if (isHeadingTitle(previous.get(1))) {
            return false;
        }

        boolean previousHasData = previous.size() > 2 && !previous.subList(2, previous.size()).stream().allMatch(String::isBlank);
        double gap = (previousLine != null && currentLine != null) ? (currentLine.centerY() - previousLine.centerY()) : 0;
        double lineHeight = (previousLine != null && currentLine != null) ? Math.max(previousLine.height(), currentLine.height()) : 12;

        if (previousHasData && (gap > lineHeight * 1.5 || gap > 18.0 || !row.get(0).isBlank())) {
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
            if (previousHasData || gap > lineHeight * 2.0) {
                return false;
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
