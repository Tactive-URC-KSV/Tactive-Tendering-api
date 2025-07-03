package com.ksv.tactive.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Region {
	
	@Id
	@Column(length = 36)
	private String id;
	
	@Column(unique = true)
	private String country;
	
	@Column(length = 10 , unique = true)
	private String countryCode;
	
	@JsonProperty("isActive")
	private boolean isActive;
	
}
