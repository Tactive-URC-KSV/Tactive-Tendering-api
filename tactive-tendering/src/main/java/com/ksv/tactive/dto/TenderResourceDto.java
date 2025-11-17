package com.ksv.tactive.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
public class TenderResourceDto {
	private String id;
	private String docNumber;
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
	private String costCodeActivityId;
	private String activityGroupId;
	private String projectId;
}
