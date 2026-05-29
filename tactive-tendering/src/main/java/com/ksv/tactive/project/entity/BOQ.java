package com.ksv.tactive.project.entity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(name = "boq", indexes = { @Index(name = "idx_project_id", columnList = "project_id"),
		@Index(name = "idx_parent_boq_id", columnList = "parent_boq_id") })
public class BOQ {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	private String boqCode;
	@Column(name = "boq_name", length = 3000)
	private String boqName;
	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "uom")
	private Uom uom;
	private int level;
	private double quantity;
	private double totalRate;
	private double totalAmount;
	private boolean lastLevel;
	@Column(nullable = true)
	private String uomCode;
	private String projectId;
	@JsonIgnore
	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "parent_boq_id")
	private BOQ parentBOQ;
	@JsonBackReference
	@JsonIgnore
	@OneToMany(mappedBy = "parentBOQ", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private List<BOQ> children = new ArrayList<>();
	
	@Transient
	private String parentBoqId;
}
