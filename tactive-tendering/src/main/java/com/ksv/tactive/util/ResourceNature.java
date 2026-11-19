package com.ksv.tactive.util;

public enum ResourceNature {
    SIMPLE("Simple"),
    COMPLEX("Complex");
    private String label;
    ResourceNature(String label) {
        this.label = label;
    }
    public String getLabel() {return label;}
}
