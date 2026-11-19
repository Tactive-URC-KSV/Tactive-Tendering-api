package com.ksv.tactive.project.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class ApprovalDocuments {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	private String documentName;
	private String filePath;
	private boolean isApproved;
	private String comments;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "technical_feasibility_id")
	private TechnicalFeasibility technicalFeasibility;
}
