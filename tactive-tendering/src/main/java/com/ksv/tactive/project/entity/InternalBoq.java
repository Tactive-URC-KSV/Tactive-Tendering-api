package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
public class InternalBoq {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne
    private Projects project;

    @ManyToOne
    private Resources resource;

    @ManyToOne
    private Uom uom;

    @ManyToOne
    private AttributeGroup attributeGroup;

    @OneToMany(mappedBy = "internalBoq", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InternalBoqAttribute> attributes = new ArrayList<>();

    private double totalQuantity;

    @OneToMany(mappedBy = "internalBoq", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InternalBoqItem> items = new ArrayList<>();

}
