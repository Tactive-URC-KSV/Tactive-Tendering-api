package com.ksv.tactive.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class Designation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String designationCode;
    private String designationName;
    private boolean active;
}
