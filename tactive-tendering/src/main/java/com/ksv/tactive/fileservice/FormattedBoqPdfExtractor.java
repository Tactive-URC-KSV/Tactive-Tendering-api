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
import java.util.Arrays;
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
        List<TextLine> lines = extractLines(document, pageIndex);
        double[] boundaries = columnBoundaries(page, rectangles, lines);
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
            // Fix PDF encoding artefact: a clause-ending word (e.g. "complete") is immediately
            // concatenated with the next capitalised word with no space in the PDF character stream.
            // Insert a single space at that boundary without altering any other logic.
            // e.g. "completeType GL-01" → "complete Type GL-01"
            if (description != null) {
                description = description.replaceAll("(?<=\\bcomplete)(?=[A-Z])", " ");
            }
            boolean underlined = underlinedLines.contains(line);
            boolean bold = line.isBold(boundaries[0], boundaries[1]);

            if (underlined) {
                foundFormattedText = true;
                appendNormalRows(normalLines, boundaries, rows);
                normalLines.clear();
                RowKind kind = bold ? RowKind.BOLD_UNDERLINE : RowKind.UNDERLINE;
                boolean startsNewRow = shouldStartNewUnderlineRow(
                        currentRow, kind, description, line.centerY(), previousLineY);
                if (startsNewRow) {
                    if (currentRow != null) {
                        rows.add(currentRow.toCells());
                    }
                    currentRow = new BoqRow(kind);
                }
                currentRow.append(1, description);
            } else {
                double gap = (previousLineY > 0) ? (line.centerY() - previousLineY) : 0;
                // A non-underlined line must NEVER continue an underlined heading row:
                // underlined rows are standalone section headings; prose below them always starts fresh.
                boolean prevIsUnderlineHeading = currentRow != null
                        && (currentRow.kind == RowKind.UNDERLINE || currentRow.kind == RowKind.BOLD_UNDERLINE);
                boolean isContinuationOfCurrentRow = !prevIsUnderlineHeading
                        && currentRow != null
                        && gap <= 22.0
                        && (cells.get(0) == null || cells.get(0).isBlank())
                        && (cells.get(2) == null || cells.get(2).isBlank())
                        && (cells.get(3) == null || cells.get(3).isBlank())
                        && (cells.get(4) == null || cells.get(4).isBlank())
                        && (cells.get(5) == null || cells.get(5).isBlank())
                        && description != null && !description.isBlank()
                        && (endsInContinuationConnector(currentRow.cells[1])
                            || Character.isLowerCase(description.trim().charAt(0))
                            || (!isHeadingTitle(currentRow.cells[1])
                                && !currentRow.cells[1].trim().endsWith(".")
                                && !currentRow.cells[1].trim().endsWith(":")));

                if (isContinuationOfCurrentRow) {
                    currentRow.append(1, description);
                } else {
                    if (currentRow != null) {
                        rows.add(currentRow.toCells());
                        currentRow = null;
                    }
                    normalLines.add(line);
                }
            }
            previousLineY = line.centerY();
        }

        appendNormalRows(normalLines, boundaries, rows);
        if (currentRow != null) {
            rows.add(currentRow.toCells());
        }
        boolean hasBoqItems = rows.stream().skip(1).anyMatch(r ->
                (r.get(0) != null && !r.get(0).isBlank())
                || (r.get(2) != null && !r.get(2).isBlank())
                || (r.get(3) != null && !r.get(3).isBlank()));
        return (foundFormattedText || !underlinedLines.isEmpty() || hasBoqItems) && rows.size() > 1
                ? rows : List.of();
    }

    private static final Set<String> CONTINUATION_WORDS = Set.of(
            "and", "or", "to", "for", "the", "of", "in", "with", "as", "by",
            "that", "which", "at", "from", "be", "under", "between", "during",
            "on", "into", "onto", "including", "such", "an", "a", "per"
    );

    private static final Set<String> OPEN_ADJECTIVES = Set.of(
            "structural", "architectural", "general", "mep", "civil", "mechanical",
            "electrical", "external", "internal", "technical", "contract", "tender",
            "approved", "specified", "standard", "particular", "relevant", "associated", "applicable",
            "aluminum", "aluminium", "side", "metal", "steel", "timber", "glazed"
    );

    private static boolean endsInContinuationConnector(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String trimmed = text.trim();
        if (trimmed.endsWith(")") || trimmed.endsWith("]") || trimmed.endsWith("}")) {
            return false;
        }
        if (trimmed.endsWith(",") || trimmed.endsWith("-") || trimmed.endsWith("–") || trimmed.endsWith("/")
                || trimmed.endsWith("+") || trimmed.endsWith("&")) {
            return true;
        }
        String[] words = trimmed.split("\\s+");
        if (words.length > 0) {
            String lastWord = words[words.length - 1].toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
            return CONTINUATION_WORDS.contains(lastWord) || OPEN_ADJECTIVES.contains(lastWord);
        }
        return false;
    }

    private static boolean shouldStartNewUnderlineRow(
            BoqRow currentRow,
            RowKind currentKind,
            String description,
            double centerY,
            double previousLineY) {
        if (currentRow == null) {
            return true;
        }
        if (currentRow.kind != currentKind) {
            return true;
        }
        double gap = (previousLineY > 0) ? (centerY - previousLineY) : 0;
        if (gap > 21.0) {
            return true;
        }
        String prevDesc = currentRow.cells[1] != null ? currentRow.cells[1].trim() : "";
        if (prevDesc.isBlank()) {
            return false;
        }
        String currDesc = description != null ? description.trim() : "";
        if (currDesc.isBlank()) {
            return false;
        }

        // 0. Explicit continuation marker like ..(cont'd) or (cont'd)
        if (currDesc.startsWith("..") || currDesc.toLowerCase(Locale.ROOT).matches("^[.(]*cont['’]?d[.)]*$")) {
            return false;
        }

        // 1. Lowercase continuation: if current line starts with lowercase, it is a continuation
        if (Character.isLowerCase(currDesc.charAt(0))) {
            return false;
        }

        // 2. Connector continuation: if previous line ended with connecting word or punctuation
        if (endsInContinuationConnector(prevDesc)) {
            return false;
        }

        // 3. Sentence terminator: if previous line ended with period, colon, or semicolon, or concluded with cont'd marker
        if (prevDesc.endsWith(".") || prevDesc.endsWith(":") || prevDesc.endsWith(";")) {
            return true;
        }
        if (prevDesc.toLowerCase(Locale.ROOT).matches(".*\\bcont['’]?d[.)]*$")) {
            return true;
        }
        if (prevDesc.toLowerCase(Locale.ROOT).matches(".*\\b(complete|completed)\\.?$")) {
            return true;
        }

        // 4. Standalone heading titles vs prose
        boolean prevIsHeading = isHeadingTitle(prevDesc);
        boolean currIsHeading = isHeadingTitle(currDesc);

        if (prevIsHeading || currIsHeading) {
            return true;
        }

        return false;
    }

    private static boolean isHeadingTitle(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String trimmed = text.trim();
        if (trimmed.contains(";")) {
            return false;
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.startsWith("the ") || lower.startsWith("allow ") || lower.startsWith("provide ")
                || lower.startsWith("to ") || lower.startsWith("for ") || lower.startsWith("all ")
                || lower.startsWith("supply") || lower.startsWith("install") || lower.startsWith("construct")
                || lower.startsWith("fabricat") || lower.startsWith("demolish") || lower.startsWith("excavat")
                || lower.startsWith("lay ") || lower.startsWith("laying ") || lower.startsWith("apply ")
                || lower.startsWith("fix ") || lower.startsWith("fixing ") || lower.startsWith("carry ")
                || lower.startsWith("remove ") || lower.startsWith("removal ") || lower.startsWith("transport")) {
            return false;
        }
        if (lower.startsWith("bill no") || lower.contains("schedule")
                || lower.equals("labour") || lower.equals("materials") || lower.equals("plant")
                || lower.contains("specification") || lower.contains("shop drawings")
                || lower.contains("insurances") || lower.contains("security")) {
            return true;
        }
        if (trimmed.endsWith(".") || trimmed.endsWith(":") || trimmed.endsWith(",")) {
            return false;
        }
        String[] words = trimmed.split("\\s+");
        return words.length <= 10;
    }

    private static final Pattern CODE_IN_DESC = Pattern.compile(
            "(?i)^([a-z]{1,3}|\\d+(?:\\.\\d+)*)[.)\\s]+(.*)$");

    private static boolean isValidExtractedCode(
            String code, String restDesc, String uom, String qty, String rate) {
        if (code == null || code.isBlank() || restDesc == null || restDesc.isBlank()) {
            return false;
        }
        String cleanCode = code.trim();
        boolean hasData = (!uom.isBlank() && !uom.matches("[-–—]+"))
                || (!qty.isBlank() && !qty.matches("[-–—]+"))
                || (!rate.isBlank() && !rate.matches("[-–—]+"));
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

    private static final class ParsedBoqLine {
        String code;
        String desc;
        String qty;
        String uom;
        String rate;
        String amount;
        double centerY;

        ParsedBoqLine(String code, String desc, String qty, String uom, String rate, String amount, double centerY) {
            this.code = code;
            this.desc = desc;
            this.qty = qty;
            this.uom = uom;
            this.rate = rate;
            this.amount = amount;
            this.centerY = centerY;
        }

        void mergeData(ParsedBoqLine other) {
            if (this.qty.isBlank() && !other.qty.isBlank()) this.qty = other.qty;
            if (this.uom.isBlank() && !other.uom.isBlank()) this.uom = other.uom;
            if (this.rate.isBlank() && !other.rate.isBlank()) this.rate = other.rate;
            if (this.amount.isBlank() && !other.amount.isBlank()) this.amount = other.amount;
        }
    }

    private void appendNormalRows(
            List<TextLine> lines, double[] boundaries, List<List<String>> rows) {
        if (lines.isEmpty()) {
            return;
        }

        int qtyCol = 2;
        int uomCol = 3;
        int rateCol = 4;
        int amountCol = 5;
        if (!rows.isEmpty()) {
            List<String> headers = rows.get(0);
            for (int i = 0; i < headers.size(); i++) {
                String h = normalize(headers.get(i));
                if (h.contains("qty") || h.contains("quant")) {
                    qtyCol = i;
                } else if (h.contains("unit") || h.contains("uom")) {
                    uomCol = i;
                } else if (h.contains("rate") || h.contains("price")) {
                    rateCol = i;
                } else if (h.contains("amount") || h.contains("total")) {
                    amountCol = i;
                }
            }
        }

        List<ParsedBoqLine> parsedLines = new ArrayList<>();
        for (TextLine line : lines) {
            List<String> cells = line.cells(boundaries);
            String col0 = cells.get(0).trim();
            String col1 = cells.get(1).trim();
            // Fix PDF encoding artefact: clause-ending word immediately concatenated with next capitalised word.
            // e.g. "completeType GL-01" → "complete Type GL-01"
            col1 = col1.replaceAll("(?<=\\bcomplete)(?=[A-Z])", " ");
            String qty  = cells.size() > qtyCol ? cells.get(qtyCol).trim() : "";
            String uom  = cells.size() > uomCol ? cells.get(uomCol).trim() : "";
            String rate = cells.size() > rateCol ? cells.get(rateCol).trim() : "";
            String amount = cells.size() > amountCol ? cells.get(amountCol).trim() : "";

            if (!qty.isBlank() && !isNumericOrDash(qty)) {
                col1 = col1.isBlank() ? qty : col1 + " " + qty;
                qty = "";
            }

            // Split merged "unit number" in the uom cell.
            // e.g. "litre 10", "m2 50", "m3 25" → uom keeps unit text, rate/qty gets the number.
            if (!uom.isBlank() && !isNumericOrDash(uom)) {
                String[] uomParts = uom.trim().split("\\s+");
                if (uomParts.length >= 2) {
                    String lastToken = uomParts[uomParts.length - 1];
                    if (lastToken.matches("\\d+(?:[,.]\\d+)?")) {
                        if (qty.isBlank()) {
                            qty = lastToken;
                        } else if (!rate.matches(".*\\d.*")) {
                            rate = lastToken;
                        }
                        uom = uom.substring(0, uom.trim().lastIndexOf(lastToken)).trim();
                    }
                }
            }
            // Normalize: clear non-numeric rate/amount
            rate   = isNumericOrDash(rate)   ? rate   : "";
            amount = isNumericOrDash(amount) ? amount : "";

            if (col0.isBlank() && !col1.isBlank()) {
                java.util.regex.Matcher m = CODE_IN_DESC.matcher(col1);
                if (m.matches()) {
                    String possibleCode = m.group(1);
                    String restDesc = m.group(2).trim();
                    if (isValidExtractedCode(possibleCode, restDesc, uom, qty, rate)) {
                        col0 = possibleCode.toUpperCase(Locale.ROOT);
                        col1 = restDesc;
                    }
                } else if (col1.matches("(?i)[a-z]")
                        && (!uom.isBlank() || !qty.isBlank() || !rate.isBlank())) {
                    col0 = col1.toUpperCase(Locale.ROOT);
                    col1 = "";
                } else if (!uom.isBlank() || !qty.isBlank() || !rate.isBlank()) {
                    java.util.regex.Matcher endMatcher = Pattern.compile("^(.*?)\\s+([B-Zb-z])$").matcher(col1);
                    if (endMatcher.matches()) {
                        String prefix = endMatcher.group(1).trim().toLowerCase(Locale.ROOT);
                        if (!prefix.endsWith("type") && !prefix.endsWith("class") && !prefix.endsWith("grade")
                                && !prefix.endsWith("phase") && !prefix.endsWith("size") && !prefix.endsWith("model")
                                && !prefix.endsWith("cat") && !prefix.endsWith("category") && !prefix.endsWith("group")) {
                            col0 = endMatcher.group(2).toUpperCase(Locale.ROOT);
                            col1 = endMatcher.group(1).trim();
                        }
                    }
                }
            } else if (!col0.isBlank() && !col1.isBlank()) {
                if (!isValidExtractedCode(col0, col1, uom, qty, rate)) {
                    col1 = col0 + " " + col1;
                    col0 = "";
                }
            }

            if (col1.isBlank() && qty.isBlank() && uom.isBlank() && rate.isBlank() && amount.isBlank()) {
                if (col0.matches("^\\d+$")) {
                    continue;
                }
            }

            parsedLines.add(new ParsedBoqLine(col0, col1, qty, uom, rate, amount, line.centerY()));
        }

        List<ParsedBoqLine> consolidated = new ArrayList<>();
        for (int i = 0; i < parsedLines.size(); i++) {
            ParsedBoqLine pl = parsedLines.get(i);
            if (pl.code.isBlank() && pl.desc.isBlank()) {
                ParsedBoqLine target = null;
                for (int j = i + 1; j < parsedLines.size() && (parsedLines.get(j).centerY - pl.centerY) <= 10.0; j++) {
                    if (!parsedLines.get(j).code.isBlank() || !parsedLines.get(j).desc.isBlank()) {
                        target = parsedLines.get(j);
                        break;
                    }
                }
                if (target == null && !consolidated.isEmpty()) {
                    ParsedBoqLine prev = consolidated.get(consolidated.size() - 1);
                    if (Math.abs(pl.centerY - prev.centerY) <= 10.0 && (!prev.code.isBlank() || !prev.desc.isBlank())) {
                        target = prev;
                    }
                }
                if (target != null) {
                    target.mergeData(pl);
                    continue;
                }
            }
            consolidated.add(pl);
        }
        parsedLines = consolidated;

        BoqRow currentRow = null;
        double previousLineY = -1;

        for (int i = 0; i < parsedLines.size(); i++) {
            ParsedBoqLine pl = parsedLines.get(i);
            // hasData = true only when there is a REAL number or meaningful unit
            boolean hasData = hasMeaningfulUnit(pl.uom)
                    || isActualNumber(pl.qty)
                    || isActualNumber(pl.rate)
                    || isActualNumber(pl.amount);
            boolean hasCode = !pl.code.isBlank();
            double gap = (previousLineY > 0) ? (pl.centerY - previousLineY) : 0;

            boolean bothHaveCode = (currentRow != null && !currentRow.cells[0].isBlank() && hasCode);
            boolean bothHaveData = (currentRow != null && currentRow.hasData() && hasData);
            if (bothHaveData && pl.code.isBlank() && gap <= 10.0) {
                boolean dataCollision = (!currentRow.cells[qtyCol].isBlank() && isActualNumber(pl.qty))
                        || (!currentRow.cells[uomCol].isBlank() && hasMeaningfulUnit(pl.uom))
                        || (!currentRow.cells[amountCol].isBlank() && isActualNumber(pl.amount));
                if (!dataCollision) {
                    bothHaveData = false;
                }
            }
            boolean gapAfterData = (currentRow != null && currentRow.hasData() && gap > 21.0);
            // A description-only continuation followed by a large gap → new row
            boolean gapBetweenDescOnly = (currentRow != null && !currentRow.hasData() && !hasData && gap > 21.0);
            boolean isOpenContinuation = endsInContinuationConnector(currentRow != null ? currentRow.cells[1] : null);
            boolean isLowercaseContinuation = pl.desc != null && !pl.desc.isBlank() && Character.isLowerCase(pl.desc.trim().charAt(0));

            // A description-only preamble row followed by a data/code line → new row (preamble stays separate)
            // UNLESS the preamble ends with a continuation connector like '+' or ',' which means it's an open item description,
            // or the incoming line has no description of its own (the preceding line is its description),
            // or the incoming line description starts with a lowercase letter (continuation of preceding description).
            boolean preambleFollowedByItem = (currentRow != null && !currentRow.hasData()
                    && currentRow.cells[0].isBlank() && (hasData || hasCode)
                    && !isOpenContinuation && !pl.desc.isBlank() && !isLowercaseContinuation);

            // Check if current line is the first line of an upcoming item whose code/data line is vertically centered
            boolean isPrecedingDescOfNextItem = false;
            if (currentRow != null && currentRow.hasData() && !hasCode && !hasData) {
                if (i + 1 < parsedLines.size()) {
                    ParsedBoqLine next = parsedLines.get(i + 1);
                    double nextGap = next.centerY - pl.centerY;
                    if (!next.code.isBlank() && nextGap <= 20.0) {
                        boolean nextIsContinuation = next.desc.isBlank()
                                || (next.desc.length() > 0 && Character.isLowerCase(next.desc.trim().charAt(0)));
                        boolean currIsNewItemDesc = pl.desc.matches("^\\d+[A-Z]?\\s+.*")
                                || !endsInContinuationConnector(currentRow.cells[1]);
                        if (nextIsContinuation || currIsNewItemDesc) {
                            isPrecedingDescOfNextItem = true;
                        }
                    }
                }
            }

            boolean startsNewRow = (currentRow == null) || bothHaveCode || bothHaveData
                    || gapAfterData || gapBetweenDescOnly || preambleFollowedByItem || isPrecedingDescOfNextItem;

            if (startsNewRow) {
                if (currentRow != null) {
                    rows.add(currentRow.toCells());
                }
                currentRow = new BoqRow(RowKind.NORMAL);
                if (hasCode) {
                    currentRow.append(0, pl.code);
                }
                currentRow.append(1, pl.desc);
                if (!pl.qty.isBlank()) currentRow.append(qtyCol, pl.qty);
                if (!pl.uom.isBlank()) currentRow.append(uomCol, pl.uom);
                if (!pl.rate.isBlank()) currentRow.append(rateCol, pl.rate);
                if (!pl.amount.isBlank()) currentRow.append(amountCol, pl.amount);
            } else {
                if (hasCode && currentRow.cells[0].isBlank()) {
                    currentRow.append(0, pl.code);
                }
                currentRow.append(1, pl.desc);
                if (!pl.qty.isBlank() && currentRow.cells[qtyCol].isBlank()) currentRow.append(qtyCol, pl.qty);
                if (!pl.uom.isBlank() && currentRow.cells[uomCol].isBlank()) currentRow.append(uomCol, pl.uom);
                if (!pl.rate.isBlank() && currentRow.cells[rateCol].isBlank()) currentRow.append(rateCol, pl.rate);
                if (!pl.amount.isBlank() && currentRow.cells[amountCol].isBlank()) currentRow.append(amountCol, pl.amount);
            }
            previousLineY = pl.centerY;
        }

        if (currentRow != null) {
            rows.add(currentRow.toCells());
        }
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
                    && !cells.get(1).isBlank() && isHeadingTitle(cells.get(1))) {
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

    private static final List<String> HEADER_KEYWORDS = List.of(
            "description", "desc", "item", "particular", "scope", "work",
            "qty", "quantity", "unit", "uom", "rate", "price", "amount",
            "total", "cost", "code", "no", "srno", "sno"
    );

    private List<String> extractHeaderCells(List<TextLine> lines, double[] boundaries) {
        int numColumns = boundaries.length + 1;
        TextLine headerLine = findBoqHeaderLine(lines);
        if (headerLine == null) {
            List<String> defaultHeaders = new ArrayList<>();
            for (int i = 1; i <= numColumns; i++) {
                defaultHeaders.add("Column " + i);
            }
            return defaultHeaders;
        }

        double headerCenter = headerLine.centerY();
        String[] headerCells = new String[numColumns];
        Arrays.fill(headerCells, "");
        lines.stream()
                .filter(line -> Math.abs(line.centerY() - headerCenter) <= 16)
                .sorted(Comparator.comparingDouble(TextLine::centerY))
                .forEach(line -> {
                    List<String> cells = line.cells(boundaries);
                    for (int i = 0; i < Math.min(headerCells.length, cells.size()); i++) {
                        if (!cells.get(i).isBlank()) {
                            headerCells[i] = headerCells[i].isBlank()
                                    ? cells.get(i)
                                    : headerCells[i] + " " + cells.get(i);
                        }
                    }
                });

        List<String> result = new ArrayList<>();
        for (int i = 0; i < headerCells.length; i++) {
            String val = headerCells[i].trim();
            result.add(val.isEmpty() ? "Column " + (i + 1) : val);
        }
        return result;
    }

    private TextLine findBoqHeaderLine(List<TextLine> lines) {
        TextLine bestKeywordLine = null;
        int maxKeywords = 0;
        for (TextLine line : lines) {
            String header = normalize(line.text());
            int count = 0;
            for (String kw : HEADER_KEYWORDS) {
                if (header.contains(kw)) {
                    count++;
                }
            }
            if (count > maxKeywords) {
                maxKeywords = count;
                bestKeywordLine = line;
            }
        }
        if (bestKeywordLine != null && maxKeywords >= 1) {
            return bestKeywordLine;
        }
        for (TextLine line : lines) {
            if (line.centerY() > 40 && line.text().trim().split("\\s+").length >= 3) {
                return line;
            }
        }
        return null;
    }

    private double[] columnBoundaries(PDPage page, List<PdfRect> rectangles, List<TextLine> lines) {
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
            return alignSerialNumberBoundary(lines, boundaries);
        }

        double width = page.getCropBox().getWidth();
        return alignSerialNumberBoundary(lines, new double[] {
                width * 0.165,
                width * 0.534,
                width * 0.59,
                width * 0.644,
                width * 0.732
        });
    }

    private double[] alignSerialNumberBoundary(List<TextLine> lines, double[] boundaries) {
        List<String> headers = extractHeaderCells(lines, boundaries);
        if (headers.size() < 2) {
            return boundaries;
        }
        String firstHeader = normalize(headers.get(0));
        String secondHeader = normalize(headers.get(1));
        boolean hasSerialHeader = secondHeader.contains("sr")
                || (secondHeader.startsWith("no")
                        && (secondHeader.contains("item") || secondHeader.contains("description")));
        if (firstHeader.contains("sr") || firstHeader.equals("no") || !hasSerialHeader
                || !(secondHeader.contains("item") || secondHeader.contains("description"))) {
            return boundaries;
        }

        TextLine headerLine = findBoqHeaderLine(lines);
        if (headerLine == null) {
            return boundaries;
        }
        double headerCenter = headerLine.centerY();
        double serialCenter = lines.stream()
                .filter(line -> Math.abs(line.centerY() - headerCenter) <= 16)
                .flatMap(line -> line.orderedRuns().stream())
                .filter(run -> normalize(run.text).equals("sr") || normalize(run.text).equals("no"))
                .mapToDouble(TextRun::centerX)
                .findFirst()
                .orElse(Double.NaN);
        double descriptionCenter = lines.stream()
                .filter(line -> Math.abs(line.centerY() - headerCenter) <= 16)
                .flatMap(line -> line.orderedRuns().stream())
                .filter(run -> normalize(run.text).contains("item")
                        || normalize(run.text).contains("description"))
                .mapToDouble(TextRun::centerX)
                .min()
                .orElse(Double.NaN);
        if (Double.isFinite(serialCenter) && Double.isFinite(descriptionCenter)
                && serialCenter < descriptionCenter && serialCenter < boundaries[1]) {
            boundaries[0] = Math.max(boundaries[0], serialCenter + 0.1);
        }
        return boundaries;
    }

    private boolean isTotalOrPageFooter(String text) {
        String normalized = normalize(text);
        if (normalized.startsWith("billno") || normalized.contains("schedule")
                || normalized.contains("material") || normalized.contains("labour") || normalized.contains("plant")) {
            return false;
        }
        return normalized.contains("totalcarried")
                || normalized.contains("grandtotal")
                || normalized.contains("carriedtosummary")
                || normalized.contains("carriedtocollection")
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

    private static boolean hasMeaningfulUnit(String value) {
        return value != null && !value.isBlank() && !value.matches("[-–—]");
    }

    private static boolean isNumericOrDash(String value) {
        return value != null && value.trim().matches("[-–—]|-?(?:\\d[\\d,]*(?:\\.\\d+)?|\\.\\d+)");
    }

    // Stricter check: real number only, not a placeholder dash
    private static boolean isActualNumber(String value) {
        return value != null && value.trim().matches("-?(?:\\d[\\d,]*(?:\\.\\d+)?|\\.\\d+)");
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

        private boolean hasData() {
            return hasMeaningfulUnit(cells[3])
                    || isActualNumber(cells[2])
                    || isActualNumber(cells[4])
                    || isActualNumber(cells[5]);
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

    static final class TextLine {
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

    record PdfRect(double minX, double minY, double maxX, double maxY) {
        boolean isHorizontalRule() {
            return maxX - minX > 10 && maxY - minY <= 2.0;
        }

        boolean isVerticalRule() {
            return maxX - minX <= 1.5 && maxY - minY > 100;
        }
    }

    static final class RectangleCollector extends PDFStreamEngine {
        private final List<PdfRect> rectangles = new ArrayList<>();

        List<PdfRect> collect(PDPage page) throws IOException {
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
