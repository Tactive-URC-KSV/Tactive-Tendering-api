package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class BOQSummaryDTO {
	private String id;
    private String boqCode;
    private String boqName;
    private Double quantity;
    private Double totalAmount;

}
