package com.ksv.tactive.project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class ContractorNature {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	private String nature;
	@ManyToOne
	@JoinColumn(name = "contractor_type_id")
	private ContractorType contractorType;
	private boolean active;

}
