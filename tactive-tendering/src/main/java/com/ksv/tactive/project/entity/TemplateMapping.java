package com.ksv.tactive.project.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class TemplateMapping {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	private String excelFields;
	private String internalFields;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "template_id")
	private Template template;
}
