package com.ksv.tactive.contractor.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class ContractorContacts {
	
	@Id
	private String id;
	private String name;
	private String designation;
	private String phoneNumber;
	private String email;
	@ManyToOne
	@JoinColumn(name = "contractor_id")
	private Contractor contractor;

}
