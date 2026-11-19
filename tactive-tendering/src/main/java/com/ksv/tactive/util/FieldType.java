package com.ksv.tactive.util;

public enum FieldType {
    TEXT("Text"),
    NUMBER("Number"),
    FILE("File"),
    DATE("Date");
    private String label;
    FieldType(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
