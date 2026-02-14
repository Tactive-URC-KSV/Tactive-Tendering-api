package com.ksv.tactive.project.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tender_estimation_attribute",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tender_estimation_id", "attribute_group_id"})
        })
public class TenderEstimationAttribute {

    @Id
    private String id;

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