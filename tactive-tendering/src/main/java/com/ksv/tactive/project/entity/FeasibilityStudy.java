package com.ksv.tactive.project.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class FeasibilityStudy {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
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
