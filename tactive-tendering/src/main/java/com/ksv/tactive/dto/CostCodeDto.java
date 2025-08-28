package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class CostCodeDto {
	
	private String activityCode;
	private String activityName;
	private double quantity;
	private double rate;
	private double amount;
	private String mappingType;
	private String uomId;
	private int boqId;
	private String costCodeTypeId;
	private String activityGroupId;
	
}
