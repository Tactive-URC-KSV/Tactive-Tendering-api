package com.ksv.tactive.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class OfferQuotation {

    @Id
    private String id;
    @ManyToOne
    @JoinColumn(name = "offer_id")
    private TenderOffers tenderOffers;
    @ManyToOne
    @JoinColumn(name = "boq_id")
    private BOQ boq;
    private double rate;
    private double quantity;
    private double amount;
}
