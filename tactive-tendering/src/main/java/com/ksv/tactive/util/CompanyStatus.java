package com.ksv.tactive.util;

public enum CompanyStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED_OPERATION("Suspended Operations"),
    RESUME("Resume");
    private String label;
    CompanyStatus(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
