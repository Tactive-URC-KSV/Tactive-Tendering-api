package com.ksv.tactive.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class ContractorAddress {
	
	@Id
	private String id;
	private String addressType;
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
