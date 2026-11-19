package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString(exclude = "resource")
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"resource_id", "attribute_group_id"}
        )
)
public class ResourceAttributeGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Version
    private Long version;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "resource_id")
    private Resources resource;
    @ManyToOne
    @JoinColumn(name = "attribute_group_id")
    private AttributeGroup attributeGroup;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "resource_attr_group_attributes",
            joinColumns = @JoinColumn(name = "rag_id"),
            inverseJoinColumns = @JoinColumn(name = "attribute_id")
    )
    private Set<Attribute> attributes = new HashSet<>();

    private int orderNo;
    private boolean mandatory;
}