package com.ksv.tactive.util;

public enum ResourceType {

    MATERIALS("Materials"),
    SUB_CONTRACTOR_WORKS("Sub-Contractor Works"),
    LABOUR_CATEGORY("Labour Category"),
    LABOUR_WORK_ITEM("Labour Work Item"),
    MACHINERY_WORK_ITEM("Machinery Work Item"),
    FORMWORK_WORK_ITEM("Formwork Work Item"),
    MACHINERY("Machinery"),
    FORMWORK("Formwork");

    private final String label;

    ResourceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
