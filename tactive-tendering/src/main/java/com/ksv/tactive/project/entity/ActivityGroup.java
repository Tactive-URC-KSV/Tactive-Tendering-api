package com.ksv.tactive.project.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class ActivityGroup {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	private String activityCode;
	private String activityName;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "cost_code_type_id")
	private CostCodeType costCodeType;
	private boolean active;
}
