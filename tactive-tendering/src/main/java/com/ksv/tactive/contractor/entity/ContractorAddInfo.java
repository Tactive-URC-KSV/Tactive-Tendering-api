package com.ksv.tactive.contractor.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class ContractorAddInfo {

	@Id
	private String id;
	private String infoType;
	private String regNo;
	@ManyToOne
	@JoinColumn(name = "contractor_id")
	private Contractor contractor;
}
