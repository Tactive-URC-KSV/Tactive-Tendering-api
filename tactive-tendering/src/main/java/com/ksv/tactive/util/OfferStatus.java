package com.ksv.tactive.util;

public enum OfferStatus {
    SUBMITTED("Submitted"),
    UNDER_EVALUATION("Under Evaluation"),
    QUALIFIED("Qualified"),
    DISQUALIFIED("Disqualified"),
    NEGOTIATION("Negotiation"),
    AWARDED("Awarded"),
    NOT_AWARDED("Not Awarded"),
    WITHDRAWN("Withdrawn");

    private String label;

    OfferStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}