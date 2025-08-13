package com.ksv.tactive.project.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "boq", indexes = { @Index(name = "idx_project_id", columnList = "project_id"),
		@Index(name = "idx_parent_boq_id", columnList = "parent_boq_id") })
public class BOQ {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private int slNo;
	private String boqCode;
	private String boqName;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "uom_id")
	private UOM uom;
	private int parentLevel;
	private int level;
	private double quantity;
	private double supplyRate;
	private double installationRate;
	private double totalRate;
	private double supplyAmount;
	private double installationAmount;
	private double totalAmount;
	private boolean lastLevel;
	private boolean rateOnly;
	private String remarks;
	private int wo;
	private String qtyType;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "project_id")
	private Projects project;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "parent_boq_id")
	private BOQ parentBOQ;

}
