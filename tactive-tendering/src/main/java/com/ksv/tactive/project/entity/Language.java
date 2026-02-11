package com.ksv.tactive.project.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
@Data
@Entity
public class Language {
	@Id
	@Column(name = "id", length = 36)
	private String id;
	private String languageCode;
	private String language;
	private boolean active;
}
