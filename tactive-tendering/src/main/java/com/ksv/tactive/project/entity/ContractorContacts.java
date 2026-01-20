package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "contractor_id")
	private Contractor contractor;

}
