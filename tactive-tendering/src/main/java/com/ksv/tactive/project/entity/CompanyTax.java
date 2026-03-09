package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CompanyTax {

    @Id
    @Column(length = 36)
    private String id;

    private String effectiveFrom;
    private String effectiveTo;
    @ManyToOne
    @JoinColumn(name = "tax_type")
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

