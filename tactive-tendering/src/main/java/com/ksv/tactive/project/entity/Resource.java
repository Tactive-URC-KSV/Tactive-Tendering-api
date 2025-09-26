package com.ksv.tactive.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Resource {
	
	@Id
	private String id;
	private String resourceCode;
	private String resourceName;
	private double unitRate;
	
	@ManyToOne
	@JoinColumn(name = "type_id")
	private ResourceType resourceType;
	
	@ManyToOne
	@JoinColumn(name = "uom_id")
	private UOM uom;
	
	@ManyToOne
	@JoinColumn(name = "quantity_type_id")
	private QuantityType quantityType;
	
	private boolean active;
	
	

}
