package com.ksv.tactive.project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class OfferNegotiation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "tender_offer_id")
    private TenderOffers tenderOffer;
    @Column(length = 1000)
    private String comments;
    private int offerVersion;
}
