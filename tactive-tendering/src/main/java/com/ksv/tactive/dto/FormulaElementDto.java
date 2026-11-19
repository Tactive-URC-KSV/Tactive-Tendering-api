package com.ksv.tactive.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FormulaElementDto {
    private String type;
    private String sourceType;
    private String refId;
    private String refCode;
    private Double value;
    private String operator;
    private int sequence;
}
