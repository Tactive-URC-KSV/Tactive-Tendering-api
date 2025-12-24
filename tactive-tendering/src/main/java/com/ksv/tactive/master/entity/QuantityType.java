package com.ksv.tactive.master.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
@Data
@Entity
public class QuantityType {
	@Id
	@Column(length = 36)
	private String id;
	private String quantityType;
	private boolean active;
}
