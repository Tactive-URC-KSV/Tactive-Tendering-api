package com.ksv.tactive.project.entity;

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
	@ManyToOne
	@JoinColumn(name = "id_type")
	private IdentityType identityType;
	private String regNo;
	@ManyToOne
	@JoinColumn(name = "contractor_id")
	private Contractor contractor;
}
