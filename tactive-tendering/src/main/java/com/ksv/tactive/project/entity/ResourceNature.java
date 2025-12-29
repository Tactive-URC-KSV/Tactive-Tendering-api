package com.ksv.tactive.project.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
@Data
@Entity
public class ResourceNature {
	@Id
	@Column(length = 36)
	private String id;
	private String nature;
	private boolean active;
}
