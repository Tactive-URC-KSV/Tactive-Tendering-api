import sys
import os
sys.path.insert(0, os.path.abspath('venv/Lib/site-packages'))
import cv2
import numpy as np

os.environ["FLAGS_use_mkldnn"] = "0"
os.environ["FLAGS_use_mkldnn_bfloat16"] = "0"
os.environ["FLAGS_use_mkldnn_int8"] = "0"

from paddleocr import PPStructure
import traceback

def test():
    try:
        # Create a dummy image
        img = np.ones((800, 600, 3), dtype=np.uint8) * 255
        
        # Draw some lines to simulate a table
        cv2.rectangle(img, (50, 50), (550, 750), (0, 0, 0), 2)
        cv2.line(img, (50, 150), (550, 150), (0, 0, 0), 2)
        
        # Initialize engine
        table_engine = PPStructure(show_log=True, image_orientation=False, enable_mkldnn=False, ir_optim=False)
        
        print("Engine initialized. Running inference...")
        result = table_engine(img)
        print("Inference successful! Result length:", len(result))
    except Exception as e:
        print("Inference failed!")
        traceback.print_exc()

if __name__ == "__main__":
    test()
