package com.crew.exception;

import java.util.Map;
import java.util.HashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import feign.FeignException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CrewMemberNotFoundException.class)
    public ResponseEntity<String> handleBadCredentialsException(CrewMemberNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(ex.getMessage());
    }
    
    @ExceptionHandler(FeignException.class)
	public ResponseEntity<String> handleFeignExceptions(FeignException ex) {
		// Handle specific Feign exceptions based on the status code
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Authorization service is unavailable");
	}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
}
