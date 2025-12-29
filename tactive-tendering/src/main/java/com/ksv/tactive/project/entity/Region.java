package com.ksv.tactive.project.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
@Data
@Entity
public class Region {
	@Id
	@Column(length = 36)
	private String id;
	@Column(unique = true)
	private String regionName;
	private boolean active;
}
