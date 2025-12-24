package com.ksv.tactive.company.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

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
	private String bank;
	@OneToOne(mappedBy = "company")
	private ComAddressDetails addressDetails;

	/* One-to-Many */
	@OneToMany(mappedBy = "company")
	private List<CompanyContact> contacts;

	@OneToMany(mappedBy = "company")
	private List<CompanyTax> taxDetails;

	@OneToMany(mappedBy = "company")
	private List<CompanyDirector> directors;

	@OneToMany(mappedBy = "company")
	private List<CompanyJointVenture> jointVentures;

	@OneToOne(mappedBy = "company")
	private CompanyProfile profile;

	@OneToMany(mappedBy = "company")
	private List<CompanyAdditionalInfo> additionalInfos;

	@OneToMany(mappedBy = "company")
	private List<CompanyLocalName> localNames;

}
