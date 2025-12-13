package com.ksv.tactive.contractor.entity;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Contractor {
	
	@Id
	private String id;
	private String entityCode;
	private String entityName;
	private Date effectiveDate;
	private String attachmentUrl;
	@ManyToOne
	@JoinColumn(name = "con_type_id")
	private ContractorType contractorType;
	@ManyToOne
	@JoinColumn(name = "con_grade_id")
	private ContractorGrade contractorGrade;
	@ManyToMany
	@JoinTable(
	    name = "contractor_nature_mapping",
	    joinColumns = @JoinColumn(name = "contractor_id"),
	    inverseJoinColumns = @JoinColumn(name = "nature_id")
	)
	private Set<ContractorNature> contractorNature = new HashSet<>();

}
