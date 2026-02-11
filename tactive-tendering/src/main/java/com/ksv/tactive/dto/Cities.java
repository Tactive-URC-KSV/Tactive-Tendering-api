package com.ksv.tactive.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class Cities {
	private String id;
	private String city;
	private boolean active;
	private States states;
	private Country country;

}
