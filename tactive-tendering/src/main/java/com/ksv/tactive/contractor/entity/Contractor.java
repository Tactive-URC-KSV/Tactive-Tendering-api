package com.ksv.tactive.contractor.entity;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Contractor {
	
	@Id
	private String id;
	private String entityName;
	private Date effectiveDate;
	

}
