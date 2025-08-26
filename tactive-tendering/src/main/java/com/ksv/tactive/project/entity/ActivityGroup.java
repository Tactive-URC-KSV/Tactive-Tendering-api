package com.ksv.tactive.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;


@Data
@Entity
public class ActivityGroup {
	
	@Id
	private String id;
	private String activityCode;
	private String activityName;
	

}
