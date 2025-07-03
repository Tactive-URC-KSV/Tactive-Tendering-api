package com.ksv.tactive.util;


public enum ProjectStatus {
	UNDER_REVISION("Under Revision"),
	PLANNING("Planning"),
	UNDER_ESTIMATION("Under Estimation"),
	TENDERING("Tendering"),
	UNDER_CONSTRUCTION("Under Construction"),
	COMPLETED("Completed"),
	CANCELLED("Cancelled");
	
	private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
