package com.ksv.tactive.project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class ContractorAddress {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	@ManyToOne
	@JoinColumn(name = "address_type")
	private AddressType addressType;
	@Column(length = 500)
	private String address1;
	@Column(length = 500)
	private String address2;
	private String country;
	private String city;
	private String zipCode;
	private String phoneNumber;
	private String email;
	@ManyToOne
	@JoinColumn(name = "contractor_id")
	private Contractor contractor;
}
