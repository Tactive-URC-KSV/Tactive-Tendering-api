package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CompanyLocalName {

    @Id
    @Column(length = 36)
    private String id;
    @ManyToOne
    @JoinColumn(name = "language")
    private Language language;
    private String name;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}

