package com.ksv.tactive.dto;

import com.ksv.tactive.project.entity.Uom;
import lombok.Data;

@Data
public class BOQDto {
    private String boqId;
    private String boqCode;
    private String boqName;
    private Uom uom;
    private double quantity;
    private double rate;
    private double amount;
    private String division;
}
