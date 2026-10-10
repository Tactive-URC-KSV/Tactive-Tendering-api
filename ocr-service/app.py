import io
import cv2
import numpy as np
import os

# Disable MKLDNN globally to prevent the "OneDnnContext does not have the input Filter" crash on CPU
os.environ["FLAGS_use_mkldnn"] = "0"
os.environ["FLAGS_use_mkldnn_bfloat16"] = "0"
os.environ["FLAGS_use_mkldnn_int8"] = "0"

from fastapi import FastAPI, UploadFile, File
from fastapi.responses import JSONResponse
from paddleocr import PPStructure
from bs4 import BeautifulSoup
try:
    import paddle.inference
    _original_switch_ir_optim = paddle.inference.Config.switch_ir_optim
    def _patched_switch_ir_optim(self, x):
        _original_switch_ir_optim(self, False)
    paddle.inference.Config.switch_ir_optim = _patched_switch_ir_optim
except ImportError:
    pass

app = FastAPI(title="PaddleOCR Table Extraction Service")

table_engine = PPStructure(show_log=False, image_orientation=False, enable_mkldnn=False, ir_optim=False)

@app.post("/extract_table")
async def extract_table(file: UploadFile = File(...)):
    try:
        contents = await file.read()
        nparr = np.frombuffer(contents, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        
        if img is None:
            return JSONResponse(status_code=400, content={"error": "Invalid image file"})
            
        result = table_engine(img)
        
        tables = []
        for region in result:
            if region['type'] == 'Table':
                html_str = region['res']['html']
                
                # Parse HTML table into a 2D list of strings
                soup = BeautifulSoup(html_str, 'html.parser')
                table_data = []
                for row in soup.find_all('tr'):
                    row_data = []
                    for cell in row.find_all(['td', 'th']):
                        # Get text, strip whitespace, handle multiline by replacing tags
                        cell_text = cell.get_text(separator=' ', strip=True)
                        row_data.append(cell_text)
                    if row_data:
                        table_data.append(row_data)
                        
                if table_data:
                    tables.append(table_data)
                    
        return {"tables": tables}
        
    except Exception as e:
        import traceback
        traceback.print_exc()
        return JSONResponse(status_code=500, content={"error": str(e)})

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
