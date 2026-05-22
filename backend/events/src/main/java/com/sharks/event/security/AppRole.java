package com.sharks.event.security;

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
		if (value == null || value.isBlank()) {
			return null;
		}
		String normalized = value.trim().toLowerCase();
		for (AppRole r : values()) {
			if (r.dbValue.equals(normalized)) {
				return r;
			}
		}
		throw new IllegalArgumentException("Unknown app role: " + value);
	}
}
