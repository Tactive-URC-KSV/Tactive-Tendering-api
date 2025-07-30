package com.ksv.tactive.project.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class TechnicalFeasibility {
	
	@Id
	@Column(length = 36)
	private String id;
	private List<String> executionCapabilities;
	
	
}
