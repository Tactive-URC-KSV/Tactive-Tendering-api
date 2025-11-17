package com.ksv.tactive.project.entity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
@Data
@Entity
public class TemplateMapping {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String excelFields;
	private String internalFields;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "template_id")
	private Template template;
}
