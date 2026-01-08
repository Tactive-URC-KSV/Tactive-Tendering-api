package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ksv.tactive.util.OfferSubmissionMode;
import com.ksv.tactive.util.TenderStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.util.Set;

@Data
@Entity
public class Tender {

    @Id
    private String id;
    private String tenderNumber;
    private String tenderName;
    private Date bidOpeningdate;
    private Date lastDate;
    private String floatedUser;
    private String contactName;
    private String contactNumber;
    private String contactEmail;
    @ManyToMany
    @JoinTable(
            name = "tender_scope",
            joinColumns = @JoinColumn(name = "tender_id"),
            inverseJoinColumns = @JoinColumn(name = "scope_id")
    )
    private Set<ScopeOfPackage> scopeOfPackages;
    @ManyToMany
    @JoinTable(
            name = "tender_boq",
            joinColumns = @JoinColumn(name = "tender_id"),
            inverseJoinColumns = @JoinColumn(name = "boq_id")
    )
    private Set<BOQ> boq;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TenderStatus tenderStatus;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Projects projects;
    @ManyToMany
    @JoinTable(
            name = "tender_contractor",
            joinColumns = @JoinColumn(name = "contractor_id"),
            inverseJoinColumns = @JoinColumn(name = "tender_id")
    )
    private Set<Contractor> contractor;
    private boolean siteInvestigation;
    private boolean preBidding;
    private Date preBiddingDate;
    private Date siteInvestigationDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "submission_mode")
    private OfferSubmissionMode submissionMode;
}
