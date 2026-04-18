package com.ksv.tactive.util;

public enum TaxType {
    REGULAR_GST("Regular GST"),
    COMPOSITE_GST("Composite GST"),
    GST_UNREGISTER(" GST Unregister"),
    INPUT_SERVICE_DISTRIBUTOR("Input Service Distributor"),
    NOT_APPLICABLE("Not Applicable");
    private String label;
    TaxType(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
