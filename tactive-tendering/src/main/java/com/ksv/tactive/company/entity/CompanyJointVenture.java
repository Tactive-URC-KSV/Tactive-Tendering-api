package com.ksv.tactive.company.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CompanyJointVenture {

    @Id
    @Column(length = 36)
    private String id;

    private String partnerId;
    private Double sharePercentage;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
