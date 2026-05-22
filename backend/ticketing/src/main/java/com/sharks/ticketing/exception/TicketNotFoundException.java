package com.sharks.ticketing.exception;

public class TicketNotFoundException extends RuntimeException {

	public TicketNotFoundException(String message) {
		super(message);
	}
}
