package com.ksv.tactive.project.entity;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
		name = "boq_tender_estimation",
		joinColumns = @JoinColumn(name = "tender_estimation_id"),
		inverseJoinColumns = @JoinColumn(name = "boq_id")
	)
	private Set<BOQ> boq;
	private double coEfficient;
	private double wastePercentage;
	private double rate;
	private double additionalRate;
	private double shippingPrice;
	private double costUnitRate;
	private boolean rateLock;
	private String resourceTypeId;
	private String resourceId;
	private String uomId;
	private String resourceNatureId;
	private String quantityTypeId;
	private String currencyId;
	private double exchangeRate;
}
