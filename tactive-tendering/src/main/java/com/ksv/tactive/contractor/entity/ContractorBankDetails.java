package com.ksv.tactive.contractor.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class ContractorBankDetails {
	
	@Id
	private String id;
	private String accHolderName;
	private String accNumber;
	private String bankName;
	private String branch;
	private String bankAddress;
	@ManyToOne
	@JoinColumn(name = "contractor_id")
	private Contractor contractor;
}
