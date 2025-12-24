package com.ksv.tactive.company.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CompanyDirector {

    @Id
    @Column(length = 36)
    private String id;

    private String directorTypeId;
    private String directorName;
    private Double sharePercentage;
    private Integer noOfShares;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
