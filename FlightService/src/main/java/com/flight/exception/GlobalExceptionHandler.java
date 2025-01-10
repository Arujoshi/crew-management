package com.flight.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import feign.FeignException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
		return ResponseEntity.badRequest().body(errors);
	}

	@ExceptionHandler(FeignException.class)
	public ResponseEntity<String> handleFeignExceptions(FeignException ex) {
		// Handle specific Feign exceptions based on the status code
		if (ex.status() == 404) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Crew member not found");
		}
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service is not available");
	}

	@ExceptionHandler(FlightNotFoundException.class)
	public ResponseEntity<String> handleFlightNotFoundExceptions(FlightNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
	}

	@ExceptionHandler(CrewConflictException.class)
	public ResponseEntity<String> handleCrewConflictExceptions(CrewConflictException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body("Crew is not available");

	}

	@ExceptionHandler(MaxWorkingHourExceedException.class)
	public ResponseEntity<String> handleMaxWorkingHourExceptions(MaxWorkingHourExceedException ex) {
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Crew member exceeds maximum working hours");

	}

	@ExceptionHandler(FlightAlreadyDepartedException.class)
	public ResponseEntity<String> handleFlightDepartedExceptions(FlightAlreadyDepartedException ex) {
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Crew member exceeds maximum working hours");

	}

	@ExceptionHandler(InsufficientRestTimeException.class)
	public ResponseEntity<String> handleInsufficientRestTimeExceptions(InsufficientRestTimeException ex) {
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Crew member exceeds maximum working hours");

	}

}
