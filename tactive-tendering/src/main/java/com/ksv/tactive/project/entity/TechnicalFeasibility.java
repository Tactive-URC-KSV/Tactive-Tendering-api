package com.ksv.tactive.project.entity;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
public class TechnicalFeasibility {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	private List<String> executionCapabilities;
}
