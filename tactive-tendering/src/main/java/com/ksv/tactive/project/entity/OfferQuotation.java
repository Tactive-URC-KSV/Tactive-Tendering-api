package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
public class OfferQuotation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "offer_id")
    private TenderOffers tenderOffers;
    @ManyToOne
    @JoinColumn(name = "boq_id")
    private BOQ boq;
    private double rate;
    private double quantity;
    private double amount;
    private int offerVersion;
    @ElementCollection
    @CollectionTable(name = "offer_payment_terms", joinColumns = @JoinColumn(name = "offer_id"))
    @Column(name = "term")
    private List<String> paymentTerms = new ArrayList<>();

}
