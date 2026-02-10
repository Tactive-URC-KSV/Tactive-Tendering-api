package com.ksv.tactive.project.entity;

import com.ksv.tactive.util.CompanyType;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
@Entity
public class Company {
	@Id
	@Column(name = "id", length = 36)
	private String id;
	private String companyName;
	private String shortName;
	private Date createdAt;
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_company_id")
	private Company parentCompany;
	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private CompanyType comType;
	@ManyToOne
	@JoinColumn(name = "com_level")
	private CompanyLevel companyLevel;
	@ManyToOne
	@JoinColumn(name = "com_nature")
	private CompanyNature companyNature;
	@ManyToOne
	@JoinColumn(name = "com_nature_of_business")
	private BusinessNature businessNature;
	@ManyToOne
	@JoinColumn(name = "com_constitution")
	private CompanyConstitution companyConstitution;
	@ManyToOne
	@JoinColumn(name = "com_status")
	private CompanyStatus companyStatus;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "language_id")
	private Language language;
	@ManyToOne
	@JoinColumn(name = "com_currency")
	private Currency currency;
	private String bank;

	@OneToOne(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private ComAddressDetails addressDetails;

	@OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CompanyContact> contacts;

	@OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CompanyTax> taxDetails;

	@OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CompanyDirector> directors;

	@OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CompanyJointVenture> jointVentures;

	@OneToOne(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private CompanyProfile profile;

	@OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CompanyAdditionalInfo> additionalInfos;

	@OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CompanyLocalName> localNames;

}
