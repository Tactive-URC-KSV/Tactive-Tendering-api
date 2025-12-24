package com.ksv.tactive.location.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Country {
	
	@Id
	@Column(length = 36)
	private String id;
	private String country;
	private String countryCode;
	private boolean active;
}
