package com.ksv.tactive.util;

public enum ContractorStatus {
    VERIFIED("Verified"),
    PENDING("Pending"),
    REJECTED("Rejected"),
    INVITED("Invited");
    private final String label;
    ContractorStatus(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
