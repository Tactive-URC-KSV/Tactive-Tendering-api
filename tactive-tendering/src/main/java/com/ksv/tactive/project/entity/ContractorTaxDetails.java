package com.ksv.tactive.project.entity;

import java.sql.Date;

import com.ksv.tactive.util.TerritoryType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class ContractorTaxDetails {
	
	@Id
	private String id;
	@ManyToOne
	@JoinColumn(name = "tax_type")
	private TaxType taxType;
	@Enumerated(EnumType.STRING)
	@Column(name = "territory_type")
	private TerritoryType territoryType;
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
