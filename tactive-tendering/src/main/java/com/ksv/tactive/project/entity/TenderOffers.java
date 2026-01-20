package com.ksv.tactive.project.entity;

import com.ksv.tactive.util.OfferStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class TenderOffers {

    @Id
    private String id;

    private int offerVersion;
    @ManyToOne
    @JoinColumn(name = "tender_id")
    private Tender tender;

    @ManyToOne
    @JoinColumn(name = "contractor_id")
    private Contractor contractor;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Projects projects;

    private Date submittedOn;

    @ElementCollection
    @CollectionTable(name = "offer_payment_terms", joinColumns = @JoinColumn(name = "offer_id"))
    @Column(name = "term")
    private List<String> paymentTerms = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OfferStatus offerStatus;

    @OneToMany(mappedBy = "tenderOffers", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<OfferQuotation> offerQuotations = new ArrayList<>();
}
