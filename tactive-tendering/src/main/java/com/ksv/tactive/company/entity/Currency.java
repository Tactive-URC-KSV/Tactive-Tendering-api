package com.ksv.tactive.company.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Currency {
	
	@Id
	@Column(length = 36)
	private String id;
	private String currencyName;
	private String currencyCode;
	private String symbol;
	private boolean isActive;
}
