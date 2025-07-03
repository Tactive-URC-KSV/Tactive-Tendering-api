package com.ksv.tactive.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class UOM {

	@Id
	@Column(length = 36)
	private String id;
	private String uomName;
	private String uomCode;
	
	
	@JsonProperty("isActive")
	private boolean isActive;
}
