package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ksv.tactive.util.TaxType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class CompanyTax {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;

    private String effectiveFrom;
    private String effectiveTo;
    @Enumerated(EnumType.STRING)
    private TaxType taxType;
    private String territoryTypeId;
    private String territory;
    private String taxRegNo;
    private String taxRegDate;
    private String address1;
    private String address2;
    private String city;
    private String pinCode;
    private String email;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}

