package com.ksv.tactive.location.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Cities {
	
	@Id
	private String id;
	private String city;
	private boolean active;
	@ManyToOne
	@JoinColumn(name = "state_id")
	private States states;
	@ManyToOne
	@JoinColumn(name = "country_id")
	private Country country;

}
