package com.ksv.tactive.project.entity;
import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
@Data
@Entity
@Table(name = "project_info")
public class Projects {
	@Id
	@Column(name = "id", nullable = false, length = 36)
	private String id;
	@Column(name = "project_name", nullable = false, length = 255)
	private String projectName;
	@Column(name = "project_code", length = 50, nullable = false, unique = true)
	private String projectCode;
	@Column(name = "short_name", length = 50, nullable = false)
	private String shortName;
	private String agreementNumber;
	private Date agreementDate;
	private double estimatedValue;
	private Date startDate;
	private Date endDate;
	private double buildingArea;
	private int numberOfFloors;
	private int numberOfAboveGround;
	private int numberOfBelowGround;
	private int carParkingFloors;
	private double ratePerUnit;
	@Column(nullable = true)
	private long phoneNo;
	private String email;
	private String city;
	private String address;
	@Column(nullable = true)
	private boolean archieve;
	private List<String> otherAmenities;
	private Date createdAt;
	private Date updateAt;
	@Column(length = 36)
	private String createdBy;
	private String projectStatusId;	
	private String regionId;
	private String sectorId;
	private Set<String> scopeOfPackages = new HashSet<>();
	private String uomId;
	@Column(length = 36)
	private String companyId;
}
