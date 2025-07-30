package com.ksv.tactive.project.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class FinancialFeasibility {

	@Id
	@Column(length = 36)
	private String id;
	private List<String> marketAvailability;
	private List<String> financialBackup;
	private double sellingCost;
	private double rentalCost;
	private double expectedProfit;
	private double profitPercentage;
	private double roiYear;
}
