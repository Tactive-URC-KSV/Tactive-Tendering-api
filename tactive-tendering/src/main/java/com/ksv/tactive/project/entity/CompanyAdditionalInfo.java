package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CompanyAdditionalInfo {

	@Id
	@Column(length = 36)
	private String id;
	@ManyToOne
	@JoinColumn(name = "id_type")
	private IdentityType identityType;
	private String registrationNo;
	@JsonBackReference
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;
}
