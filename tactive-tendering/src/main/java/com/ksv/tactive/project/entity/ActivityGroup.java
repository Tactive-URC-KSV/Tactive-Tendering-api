package com.ksv.tactive.project.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
@Data
@Entity
public class ActivityGroup {
	@Id
	private String id;
	private String activityCode;
	private String activityName;
	@ManyToOne
	@JoinColumn(name = "cost_code_type_id")
	private CostCodeType costCodeType;
}
