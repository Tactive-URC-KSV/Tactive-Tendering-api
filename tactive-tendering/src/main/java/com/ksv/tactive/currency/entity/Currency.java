package com.ksv.tactive.currency.entity;
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
	private String id;
	private String currencyName;
	private String currencyCode;
	private String symbol;
	private boolean active;
}
