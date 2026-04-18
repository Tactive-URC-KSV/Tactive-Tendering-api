package com.ksv.tactive.project.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class ProjectDocumentDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	private String documentName;
	private String documentType;
	private String documentPath;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "project_id", nullable = false)
	private Projects project;
}
