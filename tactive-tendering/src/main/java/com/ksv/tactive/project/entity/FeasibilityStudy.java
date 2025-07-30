package com.ksv.tactive.project.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Data
@Entity
public class FeasibilityStudy {

	@Id
	private String id;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "tech_feasibility_id")
	private TechnicalFeasibility technFeasibility;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "financial_fesibility_id")
	private FinancialFeasibility financialFeasibility;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "project_id")
	private Projects project;
	
	private boolean isFeasibilityApproved;
	private String comments;
	
	@Column(length = 36)
	private String reviewedBy;
	
}
