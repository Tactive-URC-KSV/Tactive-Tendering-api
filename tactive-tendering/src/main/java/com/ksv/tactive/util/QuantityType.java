package com.ksv.tactive.util;

public enum QuantityType {
    DIRECT("Direct"),
    COEFFICIENT("Co-Efficient");
    private final String label;
    QuantityType(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
