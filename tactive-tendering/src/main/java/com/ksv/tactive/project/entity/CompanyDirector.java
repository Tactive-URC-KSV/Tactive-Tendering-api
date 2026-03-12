package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ksv.tactive.util.DirectorType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class CompanyDirector {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;
    @Enumerated(EnumType.STRING)
    @Column(name = "director_type")
    private DirectorType directorType;
    private String directorName;
    private Double sharePercentage;
    private Integer noOfShares;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
