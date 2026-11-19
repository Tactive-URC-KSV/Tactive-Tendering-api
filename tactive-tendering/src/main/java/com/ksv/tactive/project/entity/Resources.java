package com.ksv.tactive.project.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ksv.tactive.util.ResourceType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@ToString(exclude = {"resourceAttributeGroups", "parent"})
public class Resources {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	private String resourceCode;
	private String resourceName;
	private boolean lastLevel;
	@Column(columnDefinition = "boolean default false")
	private boolean lastParent;
	private int level;
	@Enumerated(EnumType.STRING)
	private ResourceType resourceType;
	@ManyToOne
	@JoinColumn(name = "uom_id")
	private Uom uom;
	@JsonIgnore
	@OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
	private List<ResourceAttributeGroup> resourceAttributeGroups;
	private boolean active;
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "parent_resource")
	private Resources parent;
}
