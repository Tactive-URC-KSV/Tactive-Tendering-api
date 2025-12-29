package com.ksv.tactive.project.entity;
import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
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
	private boolean archive;
	private List<String> otherAmenities;
	private Date createdAt;
	private Date updateAt;
	@Column(length = 36)
	private String createdBy;
	@ManyToOne
	@JoinColumn(name = "status")
	private ProjectStatus projectStatus;
	@ManyToOne
	@JoinColumn(name = "region")
	private Region region;
	@ManyToOne
	@JoinColumn(name = "sector")
	private Sector sector;
	@ManyToMany
	@JoinTable(
			name = "project_scope",
			joinColumns = @JoinColumn(name = "project_id"),
			inverseJoinColumns = @JoinColumn(name = "scope_id")
	)
	private Set<ScopeOfPackage> scopeOfPackage = new HashSet<>();
	@ManyToOne
	@JoinColumn(name = "uom")
	private UOM uom;
	@Column(length = 36)
	private String companyId;
}
