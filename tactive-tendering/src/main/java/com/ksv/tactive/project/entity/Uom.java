package com.ksv.tactive.project.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
public class Uom {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	private String uomName;
	private String uomCode;
	private boolean active;
	@JsonIgnore
	@OneToMany(mappedBy = "baseUom", cascade = CascadeType.ALL)
	private List<UomConversion> conversions = new ArrayList<>();
}
