package com.gen.auth.model;

public enum AppRole {
	ATTENDEE("attendee"),
	ADMIN("admin"),
	ORGANIZER("organizer");

	private final String dbValue;

	AppRole(String dbValue) {
		this.dbValue = dbValue;
	}

	public String getDbValue() {
		return dbValue;
	}

	public static AppRole fromDbValue(String value) {
		if (value == null) {
			return null;
		}
		for (AppRole r : values()) {
			if (r.dbValue.equals(value)) {
				return r;
			}
		}
		throw new IllegalArgumentException("Unknown app_role: " + value);
	}
}
