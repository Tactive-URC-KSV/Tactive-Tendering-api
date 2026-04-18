package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@ToString(exclude = "attributes")
public class AttributeGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;
    private long groupCode;
    private String groupName;
    private boolean active;
    @JsonIgnore
    @OneToMany(mappedBy = "attributeGroup")
    private Set<Attribute> attributes;
}
