package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "tender_estimation_attribute",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tender_estimation_id", "attribute_group_id"})
        })
public class TenderEstimationAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "tender_estimation_id", nullable = false)
    private TenderEstimation tenderEstimation;

    @ManyToOne
    @JoinColumn(name = "attribute_group_id", nullable = false)
    private AttributeGroup attributeGroup;

    @ManyToOne
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;
}