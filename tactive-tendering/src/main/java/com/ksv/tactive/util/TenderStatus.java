package com.ksv.tactive.util;

public enum TenderStatus {
    PENDING("Pending"),
    NEGOTIATE("Negotiate"),
    AWARD("Award");
    private final String label;
    TenderStatus(String label){
        this.label = label;
    }
    public String getLabel(){
        return label;
    }
}
