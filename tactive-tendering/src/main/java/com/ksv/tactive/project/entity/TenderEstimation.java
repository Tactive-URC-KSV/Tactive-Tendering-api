package com.ksv.tactive.project.entity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

import com.ksv.tactive.util.QuantityType;
import com.ksv.tactive.util.ResourceNature;
import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
public class TenderEstimation {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	private String docNumber;
	
	@JsonBackReference
	@ManyToMany
	@JoinTable(
		name = "boq_tender_estimation",
		joinColumns = @JoinColumn(name = "tender_estimation_id"),
		inverseJoinColumns = @JoinColumn(name = "boq_id")
	)
	private Set<BOQ> boq;
	private double coEfficient;
	private double wastePercentage;
	private double rate;
	private double additionalRate;
	private double shippingPrice;
	private double costUnitRate;
	private boolean rateLock;
	@ManyToOne
	@JoinColumn(name = "resource_type")
	private ResourceType resourceType;
	@ManyToOne
	@JoinColumn(name = "resource")
	private Resources resources;
	@ManyToOne
	@JoinColumn(name = "uom")
	private Uom uom;
	@Enumerated(EnumType.STRING)
	@Column(name = "nature")
	private ResourceNature resourceNature;
	@Enumerated(EnumType.STRING)
	@Column(name = "quantity_type")
	private QuantityType quantityType;
	@ManyToOne
	@JoinColumn(name = "currency")
	private Currency currency;
	private double exchangeRate;
	@OneToMany(mappedBy = "tenderEstimation", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TenderEstimationAttribute> estimationAttributes = new ArrayList<>();
}
