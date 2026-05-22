package com.sharks.ticketing.exception;

public class InsufficientInventoryException extends RuntimeException {

	public InsufficientInventoryException(String message) {
		super(message);
	}
}
