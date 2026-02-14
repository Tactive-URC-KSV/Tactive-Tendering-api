package com.ksv.tactive.project.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Resources {
	@Id
	private String id;
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
