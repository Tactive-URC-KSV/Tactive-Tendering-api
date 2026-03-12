package com.ksv.tactive.project.entity;

import java.sql.Date;
import java.util.*;

import com.ksv.tactive.util.ContractorStatus;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Contractor {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	private String entityCode;
	private String entityName;
	private Date effectiveDate;
	private List<String> attachmentUrls;
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
	private String submissionMode;
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private ContractorStatus status;
	@OneToMany(mappedBy = "contractor", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ContractorContacts> contractorContacts = new ArrayList<>();
}
