package com.ksv.tactive.project.entity;

import com.ksv.tactive.util.FieldSection;
import com.ksv.tactive.util.FieldType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"field_name", "sector_id"}
        )
)
public class SectorFields {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String fieldName;
    @Enumerated(EnumType.STRING)
    private FieldType fieldType;
    private boolean mandatory;
    @ManyToOne
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;
    @Enumerated(EnumType.STRING)
    private FieldSection  fieldSection;
    private boolean active;
}
