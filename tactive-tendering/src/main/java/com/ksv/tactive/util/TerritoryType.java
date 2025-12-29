package com.ksv.tactive.util;

public enum TerritoryType {
    COUNTRY("Country"),
    CITY("City"),
    STATE("State");
    private final String label;
    TerritoryType(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
