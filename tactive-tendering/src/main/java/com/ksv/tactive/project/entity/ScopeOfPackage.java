package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class ScopeOfPackage {
	
	@Id
	@Column(length = 36)
	private String id;
	private String scope;
	
	@JsonProperty("isActive")
	private boolean isActive;
}
