package com.ksv.tactive.project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class ProjectTechFields {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length =36)
    private UUID id;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "project_id")
    private Projects project;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "tech_field_id")
    private TechFields techField;
    private String value;
}
