package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class TenderEstimationCostDetails {

	@Id
	private String id;
	
	@JsonBackReference
	@ManyToOne
    @JoinColumn(name = "tender_estimation_id")
    private TenderEstimation tenderEstimation;

    @ManyToOne
    @JoinColumn(name = "cost_code_activity_id")
    private CostCodeActivity costCodeActivity;

    private double calculatedQuantity;         
    private double wastePercentage;
    private double wasteQuantity;
    private double netQuantity;                

    private double rate;
    private double additionalRate;
    private double shippingPrice;
    private double costUnitRate;               
    private double exchangeRate;

    private double totalCostCompanyCurrency;   
    private double resourceTotalCost;          

    private double coEffSnapshot;         
}
