package com.ksv.tactive.util;

public enum Roles {
	ADMIN("admin");

	private final String displayName;

    Roles(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
