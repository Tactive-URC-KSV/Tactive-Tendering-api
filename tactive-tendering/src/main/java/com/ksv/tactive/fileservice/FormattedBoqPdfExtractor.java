package com.ksv.tactive.fileservice;

import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Service;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class FormattedBoqPdfExtractor {

    private static final Pattern ITEM_CODE = Pattern.compile(
            "(?i)(?:[a-z]{1,3}|\\d+(?:\\.\\d+)*)");

    public List<List<String>> extractRows(PDDocument document, int pageIndex) throws IOException {
        PDPage page = document.getPage(pageIndex - 1);
        List<PdfRect> rectangles = new RectangleCollector().collect(page);
        double[] boundaries = columnBoundaries(page, rectangles);
        List<TextLine> lines = extractLines(document, pageIndex);
        double headerBottom = findBoqHeaderBottom(lines);
        if (headerBottom < 0) {
            return List.of();
        }

        if (rectangles.stream().noneMatch(rectangle -> rectangle.isHorizontalRule())) {
            return List.of();
        }

        double pageHeight = page.getCropBox().getUpperRightY();
        Set<TextLine> underlinedLines = findUnderlinedLines(
                lines, rectangles, headerBottom, pageHeight, boundaries);
        addWrappedUnderlineLines(lines, underlinedLines, headerBottom, pageHeight, boundaries);
        List<List<String>> rows = new ArrayList<>();
        rows.add(extractHeaderCells(lines, boundaries));

        List<TextLine> normalLines = new ArrayList<>();
        BoqRow currentRow = null;
        double previousLineY = -1;
        boolean foundFormattedText = false;

        for (TextLine line : lines) {
            if (line.centerY() <= headerBottom + 3 || line.centerY() >= pageHeight - 60) {
                continue;
            }

            List<String> cells = line.cells(boundaries);
            String joined = String.join(" ", cells).trim();
            if (joined.isBlank() || isTotalOrPageFooter(joined) || isDecorativeFiller(joined)) {
                continue;
            }

            String description = cells.get(1);
            boolean underlined = underlinedLines.contains(line);
            boolean bold = line.isBold(boundaries[0], boundaries[1]);

            if (underlined) {
                foundFormattedText = true;
                appendNormalRows(normalLines, boundaries, rows);
                normalLines.clear();
                RowKind kind = bold ? RowKind.BOLD_UNDERLINE : RowKind.UNDERLINE;
                if (currentRow == null || currentRow.kind != kind
                        || line.centerY() - previousLineY > 21) {
                    if (currentRow != null) {
                        rows.add(currentRow.toCells());
                    }
                    currentRow = new BoqRow(kind);
                }
                currentRow.append(1, description);
            } else {
                if (currentRow != null) {
                    rows.add(currentRow.toCells());
                    currentRow = null;
                }
                normalLines.add(line);
            }
            previousLineY = line.centerY();
        }

        appendNormalRows(normalLines, boundaries, rows);
        if (currentRow != null) {
            rows.add(currentRow.toCells());
        }
        return foundFormattedText && rows.size() > 1 ? rows : List.of();
    }

    private void appendNormalRows(
            List<TextLine> lines, double[] boundaries, List<List<String>> rows) {
        if (lines.isEmpty()) {
            return;
        }

        List<TextLine> itemLines = lines.stream()
                .filter(line -> ITEM_CODE.matcher(line.cells(boundaries).get(0).trim()).matches())
                .toList();
        if (itemLines.isEmpty()) {
            BoqRow row = new BoqRow(RowKind.NORMAL);
            lines.forEach(line -> row.appendCells(line.cells(boundaries), 1));
            rows.add(row.toCells());
            return;
        }

        List<BoqRow> itemRows = itemLines.stream()
                .map(line -> {
                    BoqRow row = new BoqRow(RowKind.NORMAL);
                    row.append(0, line.cells(boundaries).get(0));
                    return row;
                })
                .toList();
        for (TextLine line : lines) {
            int nearestItem = 0;
            double nearestDistance = Double.POSITIVE_INFINITY;
            for (int i = 0; i < itemLines.size(); i++) {
                double distance = Math.abs(line.centerY() - itemLines.get(i).centerY());
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestItem = i;
                }
            }
            itemRows.get(nearestItem).appendCells(line.cells(boundaries), 1);
        }
        itemRows.forEach(row -> rows.add(row.toCells()));
    }

    private Set<TextLine> findUnderlinedLines(
            List<TextLine> lines,
            List<PdfRect> rectangles,
            double headerBottom,
            double pageHeight,
            double[] boundaries) {
        Set<TextLine> matches = Collections.newSetFromMap(new IdentityHashMap<>());
        List<PdfRect> horizontalRules = rectangles.stream()
                .filter(PdfRect::isHorizontalRule)
                .toList();
        for (TextLine line : lines) {
            if (line.centerY() <= headerBottom + 3 || line.centerY() >= pageHeight - 60) {
                continue;
            }
            String description = line.textBetween(boundaries[0], boundaries[1]);
            if (description.isBlank()) {
                continue;
            }
            double minX = line.minX(boundaries[0], boundaries[1]);
            double maxX = line.maxX(boundaries[0], boundaries[1]);
            double textWidth = maxX - minX;
            if (textWidth <= 0) {
                continue;
            }
            double bestScore = Double.NEGATIVE_INFINITY;
            for (PdfRect rectangle : horizontalRules) {
                double overlap = Math.max(0,
                        Math.min(maxX, rectangle.maxX) - Math.max(minX, rectangle.minX));
                double overlapRatio = overlap / textWidth;
                double ruleWidth = rectangle.maxX - rectangle.minX;
                double widthSimilarity = Math.min(textWidth, ruleWidth) / Math.max(textWidth, ruleWidth);
                double distance = Math.abs(
                        pageHeight - rectangle.maxY - line.bottomY(boundaries[0], boundaries[1]));
                if (overlapRatio < 0.4 || widthSimilarity < 0.65 || distance > 12) {
                    continue;
                }
                double score = widthSimilarity - distance * 0.005;
                bestScore = Math.max(bestScore, score);
            }
            if (bestScore > Double.NEGATIVE_INFINITY) {
                matches.add(line);
            }
        }
        return matches;
    }

    private void addWrappedUnderlineLines(
            List<TextLine> lines,
            Set<TextLine> underlinedLines,
            double headerBottom,
            double pageHeight,
            double[] boundaries) {
        for (int i = 0; i < lines.size(); i++) {
            TextLine line = lines.get(i);
            if (underlinedLines.contains(line)
                    || line.centerY() <= headerBottom + 3
                    || line.centerY() >= pageHeight - 60) {
                continue;
            }
            List<String> cells = line.cells(boundaries);
            if (cells.get(0).isBlank() && cells.subList(2, cells.size()).stream().allMatch(String::isBlank)
                    && !cells.get(1).isBlank()) {
                boolean adjacentMatch = hasAdjacentUnderlineLine(lines, underlinedLines, i - 1,
                        line, boundaries)
                        || hasAdjacentUnderlineLine(lines, underlinedLines, i + 1, line, boundaries);
                if (adjacentMatch) {
                    underlinedLines.add(line);
                }
            }
        }
    }

    private boolean hasAdjacentUnderlineLine(
            List<TextLine> lines,
            Set<TextLine> underlinedLines,
            int neighborIndex,
            TextLine line,
            double[] boundaries) {
        if (neighborIndex < 0 || neighborIndex >= lines.size()) {
            return false;
        }
        TextLine neighbor = lines.get(neighborIndex);
        return underlinedLines.contains(neighbor)
                && Math.abs(neighbor.centerY() - line.centerY()) <= 18
                && Math.abs(neighbor.minX(boundaries[0], boundaries[1])
                        - line.minX(boundaries[0], boundaries[1])) <= 8;
    }

    private List<TextLine> extractLines(PDDocument document, int pageIndex) throws IOException {
        List<TextRun> textRuns = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void writeString(String text, List<TextPosition> positions) {
                if (positions.isEmpty() || text == null || text.isBlank()) {
                    return;
                }
                addPositionedTextRuns(textRuns, positions);
            }
        };
        stripper.setStartPage(pageIndex);
        stripper.setEndPage(pageIndex);
        stripper.getText(document);

        List<TextRun> ordered = textRuns.stream()
                .sorted(Comparator.comparingDouble(TextRun::centerY).thenComparingDouble(TextRun::x))
                .toList();
        List<TextLine> lines = new ArrayList<>();
        for (TextRun run : ordered) {
            TextLine line = lines.stream()
                    .filter(candidate -> Math.abs(candidate.centerY() - run.centerY())
                            <= Math.max(2.0, run.height * 0.4))
                    .min(Comparator.comparingDouble(candidate ->
                            Math.abs(candidate.centerY() - run.centerY())))
                    .orElse(null);
            if (line == null) {
                line = new TextLine();
                lines.add(line);
            }
            line.add(run);
        }
        lines.sort(Comparator.comparingDouble(TextLine::centerY));
        return lines;
    }

    private void addPositionedTextRuns(List<TextRun> textRuns, List<TextPosition> positions) {
        List<TextPosition> orderedPositions = positions.stream()
                .sorted(Comparator.comparingDouble(TextPosition::getXDirAdj))
                .toList();
        StringBuilder word = new StringBuilder();
        double minX = 0;
        double maxX = 0;
        double minY = 0;
        double maxY = 0;
        String fontName = "";
        for (TextPosition position : orderedPositions) {
            String character = position.getUnicode();
            if (character == null || character.isBlank()) {
                addWordRun(textRuns, word, minX, minY, maxX, maxY, fontName);
                continue;
            }
            if (!word.isEmpty() && position.getXDirAdj() - maxX
                    > Math.max(1.2, position.getHeightDir() * 0.12)) {
                addWordRun(textRuns, word, minX, minY, maxX, maxY, fontName);
            }
            if (word.isEmpty()) {
                minX = position.getXDirAdj();
                minY = position.getYDirAdj();
                maxX = minX + position.getWidthDirAdj();
                maxY = minY + position.getHeightDir();
                fontName = position.getFont() == null ? "" : position.getFont().getName();
            } else {
                minX = Math.min(minX, position.getXDirAdj());
                minY = Math.min(minY, position.getYDirAdj());
                maxX = Math.max(maxX, position.getXDirAdj() + position.getWidthDirAdj());
                maxY = Math.max(maxY, position.getYDirAdj() + position.getHeightDir());
            }
            word.append(character);
        }
        addWordRun(textRuns, word, minX, minY, maxX, maxY, fontName);
    }

    private void addWordRun(
            List<TextRun> textRuns,
            StringBuilder word,
            double minX,
            double minY,
            double maxX,
            double maxY,
            String fontName) {
        if (!word.isEmpty()) {
            textRuns.add(new TextRun(
                    word.toString(), minX, minY, maxX - minX, maxY - minY, fontName));
            word.setLength(0);
        }
    }

    private double findBoqHeaderBottom(List<TextLine> lines) {
        TextLine itemHeader = findBoqHeaderLine(lines);
        if (itemHeader == null) {
            return -1;
        }
        double headerCenter = itemHeader.centerY();
        return lines.stream()
                .filter(line -> Math.abs(line.centerY() - headerCenter) <= 16)
                .mapToDouble(TextLine::bottomY)
                .max()
                .orElse(itemHeader.bottomY());
    }

    private List<String> extractHeaderCells(List<TextLine> lines, double[] boundaries) {
        TextLine headerLine = findBoqHeaderLine(lines);
        if (headerLine == null) {
            return List.of("S.R. NO.", "ITEM DESCRIPTION", "QTY.", "UNIT", "RATE", "AMOUNT");
        }

        double headerCenter = headerLine.centerY();
        String[] headerCells = {"", "", "", "", "", ""};
        lines.stream()
                .filter(line -> Math.abs(line.centerY() - headerCenter) <= 16)
                .sorted(Comparator.comparingDouble(TextLine::centerY))
                .forEach(line -> {
                    List<String> cells = line.cells(boundaries);
                    for (int i = 0; i < headerCells.length; i++) {
                        if (!cells.get(i).isBlank()) {
                            headerCells[i] = headerCells[i].isBlank()
                                    ? cells.get(i)
                                    : headerCells[i] + " " + cells.get(i);
                        }
                    }
                });
        return List.of(headerCells);
    }

    private TextLine findBoqHeaderLine(List<TextLine> lines) {
        for (TextLine line : lines) {
            String header = normalize(line.text());
            if (!header.contains("itemdescription")
                    || !(header.contains("qty") || header.contains("quantity"))
                    || !header.contains("unit")) {
                continue;
            }
            return line;
        }
        return null;
    }

    private double[] columnBoundaries(PDPage page, List<PdfRect> rectangles) {
        double minimumDividerHeight = page.getMediaBox().getHeight() * 0.4;
        List<Double> verticalLines = rectangles.stream()
                .filter(PdfRect::isVerticalRule)
                .filter(rectangle -> rectangle.maxY - rectangle.minY >= minimumDividerHeight)
                .map(rectangle -> (rectangle.minX + rectangle.maxX) / 2)
                .sorted()
                .collect(ArrayList::new, (values, x) -> {
                    if (values.isEmpty() || x - values.get(values.size() - 1) > 2) {
                        values.add(x);
                    }
                }, ArrayList::addAll);
        if (verticalLines.size() >= 7) {
            double[] boundaries = verticalLines.subList(1, 6)
                    .stream().mapToDouble(Double::doubleValue).toArray();
            boundaries[0] += 5;
            boundaries[1] += 5;
            return boundaries;
        }

        double width = page.getCropBox().getWidth();
        return new double[] {
                width * 0.152,
                width * 0.534,
                width * 0.59,
                width * 0.644,
                width * 0.732
        };
    }

    private boolean isTotalOrPageFooter(String text) {
        String normalized = normalize(text);
        return normalized.contains("totalcarried")
                || normalized.contains("grandtotal")
                || normalized.contains("carriedtosummary")
                || normalized.matches(".*page[billno]*\\d+.*");
    }

    private boolean isDecorativeFiller(String text) {
        return text.codePoints().allMatch(character ->
                Character.isWhitespace(character)
                        || character == '.'
                        || character == 0xFFFD
                        || character == 0x00B7
                        || character == 0x2026);
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private enum RowKind {
        NORMAL,
        UNDERLINE,
        BOLD_UNDERLINE
    }

    private static final class BoqRow {
        private final RowKind kind;
        private final String[] cells = {"", "", "", "", "", ""};

        private BoqRow(RowKind kind) {
            this.kind = kind;
        }

        private void appendCells(List<String> values, int firstCell) {
            for (int i = firstCell; i < cells.length; i++) {
                append(i, values.get(i));
            }
        }

        private void append(int index, String value) {
            if (value == null || value.isBlank()) {
                return;
            }
            cells[index] = cells[index].isBlank()
                    ? value.trim()
                    : cells[index] + " " + value.trim();
        }

        private List<String> toCells() {
            return List.of(cells);
        }

    }

    private static final class TextLine {
        private final List<TextRun> runs = new ArrayList<>();

        private void add(TextRun run) {
            runs.add(run);
        }

        private List<TextRun> orderedRuns() {
            return runs.stream().sorted(Comparator.comparingDouble(TextRun::x)).toList();
        }

        private String text() {
            return textBetween(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        }

        private String textBetween(double minX, double maxX) {
            List<TextRun> selected = orderedRuns().stream()
                    .filter(run -> run.centerX() >= minX && run.centerX() < maxX)
                    .toList();
            StringBuilder result = new StringBuilder();
            for (TextRun run : selected) {
                if (!result.isEmpty()) {
                    appendSpace(result);
                }
                result.append(run.text);
            }
            return result.toString().replace("m\uFFFD", "m²")
                    .replaceAll("\\s+", " ").trim();
        }

        private List<String> cells(double[] boundaries) {
            return List.of(
                    textBetween(Double.NEGATIVE_INFINITY, boundaries[0]),
                    textBetween(boundaries[0], boundaries[1]),
                    textBetween(boundaries[1], boundaries[2]),
                    textBetween(boundaries[2], boundaries[3]),
                    textBetween(boundaries[3], boundaries[4]),
                    textBetween(boundaries[4], Double.POSITIVE_INFINITY));
        }

        private boolean isBold(double minX, double maxX) {
            List<TextRun> descriptionRuns = orderedRuns().stream()
                    .filter(run -> run.centerX() >= minX && run.centerX() < maxX)
                    .filter(run -> !run.text.isBlank())
                    .toList();
            int boldCharacters = descriptionRuns.stream()
                    .filter(TextRun::isBold)
                    .mapToInt(run -> run.text.length())
                    .sum();
            int totalCharacters = descriptionRuns.stream().mapToInt(run -> run.text.length()).sum();
            return totalCharacters > 0 && boldCharacters >= Math.ceil(totalCharacters / 2.0);
        }

        private double centerY() {
            return runs.stream().mapToDouble(TextRun::centerY).average().orElse(0);
        }

        private double bottomY() {
            return runs.stream().mapToDouble(TextRun::bottomY).max().orElse(0);
        }

        private double bottomY(double minX, double maxX) {
            return orderedRuns().stream()
                    .filter(run -> run.centerX() >= minX && run.centerX() < maxX)
                    .mapToDouble(TextRun::bottomY)
                    .max()
                    .orElse(0);
        }

        private double minX(double min, double max) {
            return orderedRuns().stream()
                    .filter(run -> run.centerX() >= min && run.centerX() < max)
                    .mapToDouble(TextRun::x)
                    .min()
                    .orElse(0);
        }

        private double maxX(double min, double max) {
            return orderedRuns().stream()
                    .filter(run -> run.centerX() >= min && run.centerX() < max)
                    .mapToDouble(TextRun::maxX)
                    .max()
                    .orElse(0);
        }

        private static void appendSpace(StringBuilder result) {
            if (!result.isEmpty() && result.charAt(result.length() - 1) != ' ') {
                result.append(' ');
            }
        }
    }

    private record TextRun(String text, double x, double y, double width, double height, String fontName) {
        private double centerX() {
            return x + width / 2;
        }

        private double centerY() {
            return y + height / 2;
        }

        private double maxX() {
            return x + width;
        }

        private double bottomY() {
            return y + height;
        }

        private boolean isBold() {
            String normalizedFont = fontName.toLowerCase(Locale.ROOT);
            return normalizedFont.contains("bold")
                    || normalizedFont.contains("semibold")
                    || normalizedFont.contains("demi")
                    || normalizedFont.contains("black");
        }
    }

    private record PdfRect(double minX, double minY, double maxX, double maxY) {
        private boolean isHorizontalRule() {
            return maxX - minX > 10 && maxY - minY <= 0.5;
        }

        private boolean isVerticalRule() {
            return maxX - minX <= 1.5 && maxY - minY > 100;
        }
    }

    private static final class RectangleCollector extends PDFStreamEngine {
        private final List<PdfRect> rectangles = new ArrayList<>();

        private List<PdfRect> collect(PDPage page) throws IOException {
            processPage(page);
            return rectangles;
        }

        @Override
        protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
            if ("re".equals(operator.getName()) && operands.size() >= 4
                    && operands.get(0) instanceof COSNumber x
                    && operands.get(1) instanceof COSNumber y
                    && operands.get(2) instanceof COSNumber width
                    && operands.get(3) instanceof COSNumber height) {
                Point2D.Float first = transformedPoint(x.floatValue(), y.floatValue());
                Point2D.Float second = transformedPoint(x.floatValue() + width.floatValue(),
                        y.floatValue() + height.floatValue());
                rectangles.add(new PdfRect(
                        Math.min(first.x, second.x),
                        Math.min(first.y, second.y),
                        Math.max(first.x, second.x),
                        Math.max(first.y, second.y)));
            }
            super.processOperator(operator, operands);
        }
    }
}
