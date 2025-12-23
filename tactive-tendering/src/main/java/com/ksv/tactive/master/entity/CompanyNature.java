package com.ksv.tactive.master.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
@Data
@Entity
public class CompanyNature {
	@Id
	@Column(name = "id", length = 36)
	private String id;
	@Column(unique = true)
	private String comNature;
	private boolean active;
}
