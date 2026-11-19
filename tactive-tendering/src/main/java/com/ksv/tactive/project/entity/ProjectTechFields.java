package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Projects project;
    @ManyToOne
    @JoinColumn(name = "tech_field_id")
    private SectorFields techField;
    private String value;
}
