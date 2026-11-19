package com.ksv.tactive.user.entity;
import java.sql.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(name = "user_login")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	@Column(unique = true , nullable = false)
	private String email;
	@Column(unique = true , nullable = false)
	private String username;
	@Column(nullable = true)
	private long phoneNumber;
	private String name;
	@JsonIgnore
	@Column(unique = true , nullable = false)
	private String password;
	private Date createdAt;
	@JsonIgnore
	@JsonProperty("isEnabled")
	private boolean isEnabled;
	@JsonIgnore
	@Column(length = 36)
	private String companyId;
	private String role;
}
