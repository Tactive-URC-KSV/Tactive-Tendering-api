package com.ksv.tactive.project.entity;

import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "status_id")
	private ProjectStatus projectStatus;	
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "region_id")
	private Region region;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "sector_id")
	private Sector sector;
	
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(
	    name = "project_scope_packages",
	    joinColumns = @JoinColumn(name = "project_id"),
	    inverseJoinColumns = @JoinColumn(name = "scope_id")
	)
	private Set<ScopeOfPackage> scopeOfPackages = new HashSet<>();
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "uom_id")
	private UOM uom;
	
	@Column(length = 36)
	private String companyId;
	
}
