package com.ksv.tactive.company.entity;

import com.ksv.tactive.company.entity.Company;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CompanyAdditionalInfo {

	@Id
	@Column(length = 36)
	private String id;

	private String idTypeId;
	private String registrationNo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;
}
