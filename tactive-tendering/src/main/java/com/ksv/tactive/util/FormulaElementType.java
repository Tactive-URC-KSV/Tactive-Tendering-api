package com.ksv.tactive.util;

public enum FormulaElementType {
    SOURCE("Source"),
    OPERATOR("Operator"),
    NUMBER("Number");
    private String label;
    FormulaElementType(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}


