package com.ksv.tactive.master.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class AddressType {
	
	@Id
	private String id;
	private String addressType;
	private boolean active;

}
