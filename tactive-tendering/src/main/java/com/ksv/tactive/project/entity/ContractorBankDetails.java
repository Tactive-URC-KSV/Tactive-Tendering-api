package com.ksv.tactive.project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class ContractorBankDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	private String accHolderName;
	private String accNumber;
	private String bankName;
	private String branch;
	private String bankAddress;
	@ManyToOne
	@JoinColumn(name = "contractor_id")
	private Contractor contractor;
}
