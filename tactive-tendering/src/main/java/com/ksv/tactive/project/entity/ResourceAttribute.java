package com.ksv.tactive.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class ResourceAttribute {
    @Id
    private String id;
    @ManyToOne
    @JoinColumn(name = "resource_id")
    private Resources resources;
    @ManyToOne
    @JoinColumn(name = "attribute_id")
    private Attribute attribute;
    private boolean mandatory;
}
