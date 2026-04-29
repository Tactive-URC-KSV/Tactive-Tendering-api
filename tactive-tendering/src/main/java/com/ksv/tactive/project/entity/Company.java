package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ksv.tactive.util.CompanyLevel;
import com.ksv.tactive.util.CompanyNature;
import com.ksv.tactive.util.CompanyStatus;
import com.ksv.tactive.util.CompanyType;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
public class Company {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	private String companyName;
	private String shortName;
	private Date createdAt;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "parent_company_id")
	private Company parentCompany;
	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private CompanyType comType;
	@Enumerated(EnumType.STRING)
	private CompanyLevel companyLevel;
	@Enumerated(EnumType.STRING)
	@Column(name = "nature")
	private CompanyNature companyNature;
	@ManyToOne
	@JoinColumn(name = "com_nature_of_business")
	private BusinessNature businessNature;
	@ManyToOne
	@JoinColumn(name = "com_constitution")
	private CompanyConstitution companyConstitution;
	@Enumerated(EnumType.STRING)
	@Column(name = "com_status")
	private CompanyStatus companyStatus;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "language_id")
	private Language language;
	@ManyToOne
	@JoinColumn(name = "com_currency")
	private Currency currency;
	private String finStartMonth;
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ComAddressDetails> addressDetails = new ArrayList<>();

	@OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CompanyContact> contacts = new ArrayList<>();

	@OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CompanyTax> taxDetails = new ArrayList<>();

	@OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CompanyDirector> directors = new ArrayList<>();

	@OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CompanyJointVenture> jointVentures = new ArrayList<>();

	@OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CompanyProfile> profile = new ArrayList<>();

	@OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CompanyAdditionalInfo> additionalInfos = new ArrayList<>();

	@OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CompanyLocalName> localNames = new ArrayList<>();

}
