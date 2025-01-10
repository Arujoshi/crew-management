package com.flight.exception;

public class MaxWorkingHourExceedException extends RuntimeException {
	
	public MaxWorkingHourExceedException(String msg) {
		super(msg);
	}
}
