package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ksv.tactive.util.OfferStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
public class TenderOffers {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;
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

    @Enumerated(EnumType.STRING)
    private OfferStatus offerStatus;

    @OneToMany(mappedBy = "tenderOffers", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OfferQuotation> offerQuotations = new ArrayList<>();
    @JsonBackReference
    @OneToMany(mappedBy = "tenderOffer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OfferNegotiation>  offerNegotiations = new ArrayList<>();
}
