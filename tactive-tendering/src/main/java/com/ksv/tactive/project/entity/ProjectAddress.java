package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class ProjectAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String address;
    private String country;
    private String city;
    private String state;
    private long phone;
    private String email;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Projects project;
}
