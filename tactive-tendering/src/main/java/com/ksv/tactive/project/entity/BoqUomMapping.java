package com.ksv.tactive.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(
        name = "boq_uom_mapping",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "client_uom"})
)
public class BoqUomMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "client_uom", nullable = false)
    private String clientUom;

    @Column(name = "master_uom_code", nullable = false)
    private String masterUomCode;
}
