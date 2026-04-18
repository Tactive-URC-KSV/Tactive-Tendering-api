package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ksv.tactive.util.OfferSubmissionMode;
import com.ksv.tactive.util.TenderStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.sql.Time;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
public class Tender {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;
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
    private Time preBiddingTime;
    private Date siteInvestigationFromDate;
    private Date siteInvestigationToDate;
    private Time siteInvestigationFromTime;
    private Time siteInvestigationToTime;
    private String siteInvestigationComments;
    private String preBiddingComments;
    @Enumerated(EnumType.STRING)
    @Column(name = "submission_mode")
    private OfferSubmissionMode submissionMode;
    private Date floatedOn;
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "awarded_tender")
    private TenderOffers tenderOffers;
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "awarded_offer")
    private OfferQuotation offerQuotation;
}
