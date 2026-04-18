package com.ksv.tactive.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TenderResourceDto {
	private String id;
	private String docNumber;
	private int refCode;
	private double coEfficient;
	private double calculatedQuantity;
	private double wastePercentage;
	private double wasteQuantity;
	private double netQuantity;
	private double rate;
	private double additionalRate;
	private double shippingPrice;
	private double costUnitRate;
	private double resourceTotalCost;
	private boolean rateLock;
	private double exchangeRate;
	private double totalCostCompanyCurrency;
	private String resourceTypeId;
	private String quantityTypeId;
	private String resourceNatureId;
	private String uomId;
	private String currencyId;
	private String resourceId;
	private String boqId;
	private String projectId;
	private String parentId;
	private List<AttributeSelectionDto> attributes;
	private List<FormulaElementDto> formula;
}