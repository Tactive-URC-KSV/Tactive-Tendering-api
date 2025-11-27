package com.ksv.tactive.company.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
@Data
@Entity
public class ComAddressType {
	@Id
	@Column(length = 36)
	private String id;
	private String addressType;
	private boolean isActive;
}
