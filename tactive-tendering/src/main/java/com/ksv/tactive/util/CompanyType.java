package com.ksv.tactive.util;

public enum CompanyType {
    GROUP("Group"),
    COMPANY("Company");
    private final String label;
    CompanyType(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
