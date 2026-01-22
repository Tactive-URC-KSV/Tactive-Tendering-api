package com.ksv.tactive.project.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class OfferNegotiation {

    @Id
    private String id;
    @ManyToOne
    @JoinColumn(name = "tender_offer_id")
    private TenderOffers tenderOffer;
    @Column(length = 1000)
    private String comments;
    private int offerVersion;
}
