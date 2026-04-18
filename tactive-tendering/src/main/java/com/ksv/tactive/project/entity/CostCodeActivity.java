package com.ksv.tactive.project.entity;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(
	    name = "cost_code_activity",
	    indexes = { @Index(name = "idx_cost_code_activity_project_id", columnList = "project_id") }
	)
public class CostCodeActivity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	@Column(length = 1000)
	private String activityCode;
	@Column(length = 3000)
	private String activityName;
	private double quantity;
	private double rate;
	private double amount;
	private String mappingType;
	@ManyToOne
	@JoinColumn(name = "uom")
	private Uom uom;
	@JsonBackReference
	@ManyToMany
	@JoinTable(
		    name = "activity_boq",
		    joinColumns = @JoinColumn(name = "activity_id"),
		    inverseJoinColumns = @JoinColumn(name = "boq_id")
		)
	private Set<BOQ>  boq;
	@ManyToOne
	@JoinColumn(name="cost_code_type_id")
	private CostCodeType costCodeType;
	@ManyToOne
	@JoinColumn(name = "activity_group_id")
	private ActivityGroup activityGroup;
	@JsonBackReference
	@ManyToOne
	@JoinColumn(name = "project_id")
	private Projects project;
	@Column(nullable = true)
	private double resourceRate;
	@Column(nullable = true)
	private double resourceAmount;
	
}
