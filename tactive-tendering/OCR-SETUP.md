# Java OCR Table Extraction

The Java API uses Tess4J/Tesseract to extract BOQ tables from scanned PDF pages. For PDFs with selectable text, it first attempts text and table extraction with PDFBox and Tabula, then uses OCR when those methods do not produce a usable table. OCR output is normalized into six columns: serial number, item description, quantity, unit, rate, and amount.

## Requirements

1. Java 21.
2. The API's configured PostgreSQL databases and other application settings.
3. Maven Wrapper included in this project.

## Setup

The OCR library is included as a Maven dependency. No separate Python service or Tesseract installation is required when the configured language model is available to the application.

By default, the application uses the `eng` language. To use a model from a local `tessdata` directory, set `OCR_TESSDATA_PATH` to that directory. The directory must contain the selected language file, such as `eng.traineddata`. Set `ocr.tessdata.language` in the application configuration to select a different language.

If no external path is configured, the application looks for the selected language model on its classpath and extracts it to a temporary directory for OCR. If the model cannot be found, OCR reports a configuration error.

## Running the API

From the `tactive-tendering` project directory, run:

```powershell
.\mvnw.cmd spring-boot:run
```

## Using OCR

OCR is integrated into the existing PDF BOQ extraction and import flow; it does not run as a separate service or expose a standalone OCR endpoint. Upload a PDF through the BOQ workflow. The PDF upload screen accepts an inclusive start and end page, and preview extraction and final import use the selected page range.

The extracted table uses these columns:

| Column | Description |
|---|---|
| Serial number | BOQ item number |
| Item description | BOQ item description |
| Quantity | Item quantity |
| Unit | Unit of measurement |
| Rate | Item rate |
| Amount | Item amount |
