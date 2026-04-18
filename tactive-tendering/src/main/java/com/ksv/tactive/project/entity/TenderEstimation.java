package com.ksv.tactive.project.entity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ksv.tactive.util.QuantityType;
import com.ksv.tactive.util.ResourceNature;
import com.ksv.tactive.util.ResourceType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class TenderEstimation {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	private String docNumber;
	@ManyToOne
	@JoinColumn(name = "boq_id")
	private BOQ boq;
	@Enumerated(EnumType.STRING)
	private ResourceType resourceType;
	@ManyToOne
	private Resources resources;
	@ManyToOne
	private Uom uom;
	@Enumerated(EnumType.STRING)
	private ResourceNature resourceNature;
	@Enumerated(EnumType.STRING)
	private QuantityType quantityType;
	private int refCode;
	@ManyToOne
	private Currency currency;
	private double coEfficient;
	private double wastePercentage;
	private double calculatedQuantity;
	private double wasteQuantity;
	private double netQuantity;
	private double rate;
	private double additionalRate;
	private double shippingPrice;
	private double costUnitRate;
	private double exchangeRate;
	private double totalCostCompanyCurrency;
	private double resourceTotalCost;
	private boolean rateLock;
	@OneToMany(mappedBy = "tenderEstimation", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TenderEstimationAttribute> estimationAttributes = new ArrayList<>();
	@ManyToOne
	@JoinColumn(name = "parent_id")
	private TenderEstimation parent;
	@ManyToOne
	@JoinColumn(name = "project_id")
	private Projects project;
	@OneToMany(mappedBy = "tenderEstimation", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TenderFormulaElement> formulaElements = new ArrayList<>();
	@JsonIgnore
	@OneToMany(mappedBy = "tenderEstimation", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InternalBoqItem> internalBoqItems = new ArrayList<>();
}
