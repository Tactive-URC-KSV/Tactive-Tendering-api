package com.ksv.tactive.project.entity;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.*;

import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(name = "project_info")
public class Projects {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	@Column(name = "project_name", nullable = false, length = 255)
	private String projectName;
	@Column(name = "project_code", length = 50, nullable = false, unique = true)
	private String projectCode;
	@Column(name = "short_name", length = 50, nullable = false)
	private String shortName;
	private Date startDate;
	private Date endDate;
	@Column(name = "total_estimated_value", precision = 15, scale = 2)
	private BigDecimal totalEstimatedValue = BigDecimal.ZERO;
	@Column(nullable = true)
	private boolean archive;
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
	private Uom uom;
	@Column(length = 36)
	private String companyId;
	private boolean needFeasibility;

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProjectAddress> projectAddresses = new ArrayList<>();

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProjectTechFields> techFields = new ArrayList<>();

}
