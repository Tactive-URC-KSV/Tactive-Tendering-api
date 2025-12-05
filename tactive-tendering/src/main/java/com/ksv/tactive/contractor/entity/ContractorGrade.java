package com.ksv.tactive.contractor.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class ContractorGrade {
	
	@Id
	private String id;
	private String gradeName;
	private boolean active;
}
