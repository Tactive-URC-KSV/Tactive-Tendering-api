package com.ksv.tactive.util;

public enum CompanyNature {
    REGISTERED("Registered"),
    UN_REGISTERED("Un Registered");
    private final String label;
    CompanyNature(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
