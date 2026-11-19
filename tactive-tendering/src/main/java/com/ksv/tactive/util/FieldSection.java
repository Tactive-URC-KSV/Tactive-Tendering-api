package com.ksv.tactive.util;

public enum FieldSection {
    TECH("Techical Field"),
    ROI("Return on Investment(ROI)"),
    DOCUMENTS("Documents");
    private String label;
    FieldSection(String label){
        this.label = label;
    }
    public String getLabel(){
        return label;
    }
}
