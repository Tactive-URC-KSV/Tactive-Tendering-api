package com.ksv.tactive.project.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class ApprovalDocuments {
	
	@Id
	private String id;
	private String documentName;
	private String filePath;
	private boolean isApproved;
	private String comments;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "technical_feasibility_id")
	private TechnicalFeasibility technicalFeasibility;
}
