package com.ksv.tactive.project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class ContractorAddInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	@ManyToOne
	@JoinColumn(name = "id_type")
	private IdentityType identityType;
	private String regNo;
	@ManyToOne
	@JoinColumn(name = "contractor_id")
	private Contractor contractor;
}
