package com.ksv.tactive.project.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Entity
public class Resources {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	private String resourceCode;
	private String resourceName;
	private double unitRate;
	@ManyToOne
	@JoinColumn(name = "type_id")
	private ResourceType resourceType;
	@ManyToOne
	@JoinColumn(name = "uom_id")
	private Uom uom;
	@JsonBackReference
	@OneToMany(mappedBy = "resources", cascade = CascadeType.ALL)
	private List<ResourceAttribute> resourceAttributes;
	private boolean active;
}
