package com.ksv.tactive.project.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
public class ComAddressDetails {
	@Id
	private String id;
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
	@ManyToOne
	@JoinColumn(name = "address_type")
	private AddressType addressType;
	@JsonBackReference
	@OneToOne
	@JoinColumn(name = "company_id")
	private Company company;
}
