package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CompanyContact {

    @Id
    @Column(length = 36)
    private String id;

    private String position;
    private String name;
    private String phoneNo;
    private String email;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
