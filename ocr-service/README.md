# PaddleOCR Table Extraction Service

A lightweight FastAPI service for extracting tables from images (e.g. rendered PDF pages) using PaddleOCR's PP-Structure model.

## Setup

1. Install Python 3.8+ (preferably).
2. Install the required dependencies:
   ```bash
   pip install -r requirements.txt
   ```

## Running the Service

```bash
uvicorn app:app --host 0.0.0.0 --port 2088
```

## API Endpoint

`POST /extract_table`
- **Content-Type**: `multipart/form-data`
- **Body**: `file` (image file)

Returns a JSON object containing the extracted tables:
```json
{
  "tables": [
    [
      ["header1", "header2"],
      ["value1", "value2"]
    ]
  ]
}
```
