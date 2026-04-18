package com.ksv.tactive.util;

public enum FeasibilityMaster {
    MARKET_AVAILABILITY("Market Availability"),
    EXECUTION_CAPABILITY("Execution Capability"),
    FINANCIAL_BACKUP("Financial Backup");
    private String label;
    FeasibilityMaster(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
