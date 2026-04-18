package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Entity
public class TenderAttachments {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;
    private List<String> technicalTermsUrl;
    private String technicalTerms;
    private String commercialTerms;
    private List<String> commercialTermsUrl;
    private List<String> drawingUrl;
    private List<String> otherUrl;
    private String drawings;
    private String others;
    @JsonBackReference
    @OneToOne
    @JoinColumn(name = "tender_id")
    private Tender tender;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Projects project;
}
