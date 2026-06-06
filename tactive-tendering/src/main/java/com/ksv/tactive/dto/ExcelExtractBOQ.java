package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class ExcelExtractBOQ {
	
	private long sno;
	private String boqCode;
	private String boqName;
	private String uom;
	private Double quantity;
	private int level;
	private long parentSno;
	private boolean lastLevel;
	private String division;
	private Long pageNo;
	
}
