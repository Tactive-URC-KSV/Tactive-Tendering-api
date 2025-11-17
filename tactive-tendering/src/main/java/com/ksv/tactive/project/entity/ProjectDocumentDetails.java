package com.ksv.tactive.project.entity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
@Data
@Entity
public class ProjectDocumentDetails {
	@Id
	@Column(length = 36)
	private String id;
	private String documentName;
	private String documentType;
	private String documentPath;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "project_id", nullable = false)
	private Projects project;
}
