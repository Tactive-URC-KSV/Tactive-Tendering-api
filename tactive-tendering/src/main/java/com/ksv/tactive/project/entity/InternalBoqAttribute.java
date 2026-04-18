package com.ksv.tactive.project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class InternalBoqAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private InternalBoq internalBoq;

    @ManyToOne
    private Attribute attribute;
}