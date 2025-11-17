package com.ksv.tactive.util;
public enum Months {
    JANUARY("January"),
    FEBRUARY("February"),
    MARCH("March"),
    APRIL("April"),
    MAY("May"),
    JUNE("June"),
    JULY("July"),
    AUGUST("August"),
    SEPTEMBER("September"),
    OCTOBER("October"),
    NOVEMBER("November"),
    DECEMBER("December");
    private final String label;
    Months(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
