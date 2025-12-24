package com.ksv.tactive.company.entity;
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
	private String addressTypeId;
	@OneToOne
	@JoinColumn(name = "company_id")
	private Company company;
}
