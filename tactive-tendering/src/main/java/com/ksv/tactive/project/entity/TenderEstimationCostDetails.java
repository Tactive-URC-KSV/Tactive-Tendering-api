package com.ksv.tactive.project.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class TenderEstimationCostDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;
	@JsonBackReference
	@ManyToOne
    @JoinColumn(name = "tender_estimation_id")
    private TenderEstimation tenderEstimation;
    @ManyToOne
    @JoinColumn(name = "boq_id")
    private BOQ boq;
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
    private boolean rateLock;
}
