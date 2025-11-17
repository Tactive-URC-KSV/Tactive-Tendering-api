package com.ksv.tactive.project.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
@Data
@Entity
public class Template {
	@Id
	private String id;
	private String templateCode;
	private String templateName;
	@Column(length = 1000)
	private String description;
}
