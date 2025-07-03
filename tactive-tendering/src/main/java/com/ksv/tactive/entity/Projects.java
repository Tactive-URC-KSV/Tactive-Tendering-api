package com.ksv.tactive.entity;

import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ksv.tactive.util.ProjectStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
	private Date StartDate;
	private Date endDate;
	private double buildingArea;
	private int numberOfFloors;
	private int numberOfAboveGround;
	private int numberOfBelowGround;
	private int carParkingFloors;
	private double ratePerUnit;
	
	private String city;
	private String address;
	
	private List<String> otherAmenities;
	
	@Enumerated(EnumType.STRING)
	private ProjectStatus projectStatus;	
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "region_id")
	private Region region;
	
	@ManyToOne()
	@JoinColumn(name = "sector_id")
	private Sector sector;
	
	@ManyToMany
	@JoinTable(
	    name = "project_scope_packages",
	    joinColumns = @JoinColumn(name = "project_id"),
	    inverseJoinColumns = @JoinColumn(name = "scope_id")
	)
	private Set<ScopeOfPackage> scopeOfPackages = new HashSet<>();
	
	@ManyToOne
	@JoinColumn(name = "uom_id")
	private UOM uom;
	
	
}
