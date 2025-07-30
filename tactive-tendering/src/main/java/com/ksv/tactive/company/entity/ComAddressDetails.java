package com.ksv.tactive.company.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Data
@Entity
public class ComAddressDetails {
	
	@Id
	private String id;
	
	@Column(length = 1000)
	private String address1;
	
	@Column(length = 1000)
	private String address2;
	
	private String zipcode;
	private String email;
	private String website;
	private String faxNo;
	private String phone;
	
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "address_type")
	private ComAddressType addressType;
	
	
	
	
	
}
