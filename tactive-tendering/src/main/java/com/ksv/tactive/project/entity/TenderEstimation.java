package com.ksv.tactive.project.entity;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class TenderEstimation {
	
	@Id
	private String id;
	private String docNumber;
	
	@JsonBackReference
	@ManyToMany
	@JoinTable(
		name = "tender_estimation_cost_code_activities",
		joinColumns = @JoinColumn(name = "tender_estimation_id"),
		inverseJoinColumns = @JoinColumn(name = "cost_code_activity_id")
	)
	private Set<CostCodeActivity> costCodeActivity;
	
	@JsonBackReference
	@ManyToMany
	@JoinTable(
		name = "tender_estimation_activity_group",
		joinColumns = @JoinColumn(name = "tender_estimation_id"),
		inverseJoinColumns = @JoinColumn(name = "activity_group_id")
	)
	private Set<ActivityGroup> activityGroup;
	
	private double coEfficient;
	private double wastePercentage;
	private double rate;
	private double additionalRate;
	private double shippingPrice;
	private double costUnitRate;
	private boolean rateLock;
	
	@ManyToOne
	@JoinColumn(name = "resource_type_id")
	private ResourceType resourceType;
	
	@ManyToOne 
	@JoinColumn(name = "resource_id")
	private Resources resource;
	
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
	
	private double exchangeRate;
	
}
