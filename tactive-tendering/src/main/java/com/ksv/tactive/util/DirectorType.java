package com.ksv.tactive.util;

public enum DirectorType {
    PART_TIME("Part Time"),
    FULL_TIME("Full Time");
    private final String label;
    DirectorType(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
