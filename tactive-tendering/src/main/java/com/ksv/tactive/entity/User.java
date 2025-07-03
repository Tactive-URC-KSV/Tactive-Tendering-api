package com.ksv.tactive.entity;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ksv.tactive.util.Roles;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "user_login")
public class User {
	
	@Id
	@Column(length = 36)
	private String id;
	
	@Column(unique = true , nullable = false)
	private String email;
	
	@Column(unique = true , nullable = false)
	private String username;
	
	@Column(unique = true , nullable = false)
	private String password;
	
	private Timestamp createdAt;
	
	@Enumerated(EnumType.STRING)
	private Roles role;
	
	@JsonProperty("isEnabled")
	private boolean isEnabled;
	
	
}
