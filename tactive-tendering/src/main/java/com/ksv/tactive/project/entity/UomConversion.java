package com.ksv.tactive.project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"base_uom_id", "conversion_uom_id"}
        )
)
public class UomConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "base_uom_id", nullable = false)
    private Uom baseUom;
    @ManyToOne
    @JoinColumn(name = "conversion_uom_id", nullable = false)
    private Uom conversionUom;
    @Column(nullable = false)
    private String formula;

}
