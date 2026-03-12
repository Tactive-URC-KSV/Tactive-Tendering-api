package com.ksv.tactive.project.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class ComAddressDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private UUID id;
	@Column(length = 1000)
	private String address1;
	@Column(length = 1000)
	private String address2;
	private String zipcode;
	private String email;
	private String website;
	private String faxNo;
	private String phone;
	private boolean primary;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "address_type")
	private AddressType addressType;
	@JsonBackReference
	@OneToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "company_id")
	private Company company;
}
