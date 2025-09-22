package com.ksv.tactive.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class TenderEstimation {
	
	@Id
	private String id;
	private String docNumber;
	
	@ManyToOne
	@JoinColumn(name = "cost_code_activity_id")
	private CostCodeActivity costCodeActivity;
	
	@ManyToOne
	@JoinColumn(name = "activity_group_id")
	private ActivityGroup activityGroup;
	
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
	
	@ManyToOne
	@JoinColumn(name = "resource_type_id")
	private ResourceType resourceType;
	
	@ManyToOne 
	@JoinColumn(name = "resource_id")
	private Resource resource;
	
	@ManyToOne
	@JoinColumn(name = "uom_id")
	private UOM uom;
	
	@ManyToOne
	@JoinColumn(name = "resource_nature_id")
	private ResourceNature resourceNature;
	
	@ManyToOne
	@JoinColumn(name = "quantity_type_id")
	private QuantityType quantityType;
	
	@ManyToOne
	@JoinColumn(name = "currency_id")
	private Currency currency;
	
	private double totalCostCompanyCurrency;
	

}
