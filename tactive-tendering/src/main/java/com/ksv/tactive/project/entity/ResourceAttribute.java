package com.ksv.tactive.project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class ResourceAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "resource_id")
    private Resources resources;
    @ManyToOne
    @JoinColumn(name = "attribute_id")
    private AttributeGroup attributeGroup;
    private boolean mandatory;
}
