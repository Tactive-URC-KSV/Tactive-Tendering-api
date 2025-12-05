package com.ksv.tactive.contractor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class ContractorType {
	
	@Id
	@Column(length = 36)
	private String id;
	private String type;
	private boolean active;

}
