package com.ksv.tactive.company.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;
@Data
@Entity
public class Company {
	@Id
	@Column(name = "id", length = 36)
	private String id;
	private String companyName;
	private String shortName;
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_company_id")
	private Company parentCompany;
	private String comTypeId;
	private String comLevelId;
	private String comNatureId;
	private String businessNatureId;
	private String companyConstitutionId;
	private String statusId;
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "language_id")
	private Language language;
	private String currencyId;
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "bank")
	private Bank bank;
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "address_details")
	private ComAddressDetails addressDetails;
}
