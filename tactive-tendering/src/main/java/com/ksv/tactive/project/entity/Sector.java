package com.ksv.tactive.project.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class Sector {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	@Column(unique = true)
	private String sectorName;
	private boolean active;
}
