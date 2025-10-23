package com.ksv.tactive.dto;

import com.ksv.tactive.project.entity.TenderEstimation;
import com.ksv.tactive.project.entity.TenderEstimationCostDetails;

import lombok.Data;

@Data
public class TenderEstResourceDto {
	
	private TenderEstimation tenderEstimation;
	private TenderEstimationCostDetails costDetails;

}
