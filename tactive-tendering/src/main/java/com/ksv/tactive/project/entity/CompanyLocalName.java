package com.ksv.tactive.project.entity;

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

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}

