package com.ksv.tactive.util;

public enum CompanyLevel {
    FIRST_LEVEL("First Level"),
    SECOND_LEVEL("Second Level");
    private final String label;
    CompanyLevel(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
