package com.ksv.tactive.company.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class CompanyProfile {

    @Id
    @Column(length = 36)
    private String id;

    private Integer orderNo;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    private List<String> attachmentUrl;

    @OneToOne
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;
}
