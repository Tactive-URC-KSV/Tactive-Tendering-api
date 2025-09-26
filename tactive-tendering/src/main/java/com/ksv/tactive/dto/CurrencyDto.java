package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class CurrencyDto {
	
	private String currencyName;
	private String currencyCode;
	private String symbol;
	private boolean isActive;
}
