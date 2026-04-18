package com.ksv.tactive.dto;

import com.ksv.tactive.project.entity.TenderEstimation;
import lombok.Data;

@Data
public class TenderEstResourceDto {
    private TenderEstimation tenderEstimation;
    private double calculatedQuantity;
    private double wasteQuantity;
    private double netQuantity;
    private double totalCostCompanyCurrency;
    private double resourceTotalCost;
    private double coEfficient;
}