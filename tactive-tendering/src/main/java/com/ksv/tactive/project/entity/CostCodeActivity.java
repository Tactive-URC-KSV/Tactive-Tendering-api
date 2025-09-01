package com.ksv.tactive.project.entity;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;


@Data
@Entity
@Table(
	    name = "cost_code_activity",
	    indexes = { @Index(name = "idx_cost_code_activity_project_id", columnList = "project_id") }
	)

public class CostCodeActivity {

	@Id
	private String id;
	private String activityCode;
	private String activityName;
	private double quantity;
	private double rate;
	private double amount;
	private String mappingType;
	
	@ManyToOne
	@JoinColumn(name = "uom_id")
	private UOM uom;
	
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
	
	@ManyToOne
	@JoinColumn(name = "project_id")
	private Projects project;
}
