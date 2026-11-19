package com.ksv.tactive.project.entity;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
public class FinancialFeasibility {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	private List<String> marketAvailability;
	private List<String> financialBackup;
	private double sellingCost;
	private double rentalCost;
	private double expectedProfit;
	private double profitPercentage;
	private double roiYear;
}
