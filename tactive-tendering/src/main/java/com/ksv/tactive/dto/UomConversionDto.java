package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class UomConversionDto {
    private String id;
    private String baseUomId;
    private String conversionUomId;
    private String formula;
}
