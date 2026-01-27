package com.ksv.tactive.dto;

import com.ksv.tactive.project.entity.UOM;
import lombok.Data;

@Data
public class BOQDto {
    private String boqId;
    private String boqCode;
    private String boqName;
    private UOM uom;
    private double quantity;
    private double rate;
    private double amount;
}
