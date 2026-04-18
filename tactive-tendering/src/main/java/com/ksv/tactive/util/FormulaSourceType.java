package com.ksv.tactive.util;

public enum FormulaSourceType {
    BOQ("Boq"),
    RESOURCE("Resource"),
    GLOBAL("Global");
    private String label;
    FormulaSourceType(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
