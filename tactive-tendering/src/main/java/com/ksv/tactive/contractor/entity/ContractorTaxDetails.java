package com.ksv.tactive.contractor.entity;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class ContractorTaxDetails {
	
	@Id
	private String id;
	private String taxType;
	private String territoryType;
	private String territory;
	private String taxRegNumber;
	private Date taxRegDate;
	@Column(length = 500)
	private String address1;
	@Column(length = 500)
	private String address2;
	private String city;
	private String pinCode;
	private String email;
	@ManyToOne
	@JoinColumn(name = "contractor_id")
	private Contractor contractor;
	

}
