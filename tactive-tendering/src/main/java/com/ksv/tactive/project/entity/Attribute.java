package com.ksv.tactive.project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Data
@Entity
public class Attribute {
    @Id
    private String id;
    private String attributeName;
    private boolean active;
    @ManyToMany
    @JoinTable(
            name = "attribute_attribute_group",
            joinColumns = @JoinColumn(name = "attribute_id"),
            inverseJoinColumns = @JoinColumn(name = "attribute_group_id")
    )
    private Set<AttributeGroup> attributeGroup;

}
