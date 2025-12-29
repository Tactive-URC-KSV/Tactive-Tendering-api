package com.ksv.tactive.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class ContractorNature {
	
	@Id
	private String id;
	private String nature;
	@ManyToOne
	@JoinColumn(name = "contractor_type_id")
	private ContractorType contractorType;
	

}
