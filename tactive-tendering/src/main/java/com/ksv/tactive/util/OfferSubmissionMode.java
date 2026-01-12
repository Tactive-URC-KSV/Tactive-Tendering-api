package com.ksv.tactive.util;

public enum OfferSubmissionMode {
    ONLINE("Online"),
    OFFLINE("Offline");
    private String label;
    OfferSubmissionMode(String label){
        this.label = label;
    }
    public String getLabel(){
        return label;
    }
}
