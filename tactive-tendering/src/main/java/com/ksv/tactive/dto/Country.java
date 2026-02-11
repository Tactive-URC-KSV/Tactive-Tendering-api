package com.ksv.tactive.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class Country {

	private String id;
	private String country;
	private String countryCode;
	private String phoneNumberCode;
	private boolean active;
}
