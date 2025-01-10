package com.flight.exception;

public class FlightAlreadyDepartedException extends RuntimeException {

	public FlightAlreadyDepartedException(String msg) {
		super(msg);
	}
}
