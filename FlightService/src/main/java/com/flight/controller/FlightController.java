package com.flight.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flight.exception.CrewConflictException;
import com.flight.exception.MaxWorkingHourExceedException;
import com.flight.feign.AuthorisationClient;
import com.flight.model.CrewAssignment;
import com.flight.model.Flight;
import com.flight.request.FlightAssignmentRequest;
import com.flight.service.FlightAssignmentService;
import com.flight.service.FlightServiceImpl;

import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/flight")
public class FlightController {

	@Autowired
	private FlightServiceImpl flightService;

	@Autowired
	private FlightAssignmentService flightAssignment;

	@Autowired
	private AuthorisationClient authorisationClient;

	@GetMapping
	public ResponseEntity<?> getAllFligts(@RequestHeader(name = "Authorization") String token) {
		if (authorisationClient.validate(token)) {
			return new ResponseEntity<>(flightService.getAllFlights(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Authontication Required", HttpStatus.FORBIDDEN);
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getFlightById(@RequestHeader(name = "Authorization") String token,
			@PathVariable("id") Long id) {
		Flight flight = flightService.getFlightById(id);
		if (authorisationClient.validate(token)) {
			return new ResponseEntity<>(flight, HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Authontication Required", HttpStatus.FORBIDDEN);
		}
	}

	@PostMapping
	public ResponseEntity<?> addNewFlight(@RequestHeader(name = "Authorization") String token,
			@Valid @RequestBody Flight flight) {
		System.out.println(token);
		if (authorisationClient.validate(token)) {
			return new ResponseEntity<>(flightService.addFlight(flight), HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>("Authontication Required", HttpStatus.FORBIDDEN);
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteFlight(@RequestHeader(name = "Authorization") String token,
			@PathVariable("id") Long id) {
		if (authorisationClient.validate(token)) {
			return new ResponseEntity<>(flightService.deleteFlight(id), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Authontication Required", HttpStatus.FORBIDDEN);
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateFlight(@RequestHeader(name = "Authorization") String token,
			@PathVariable("id") Long id, @Valid @RequestBody Flight flight) {
		if (authorisationClient.validate(token)) {
			return new ResponseEntity<>(flightService.updateFlight(id, flight), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Authontication Required", HttpStatus.FORBIDDEN);
		}
	}

	@PostMapping("/assign")
	public ResponseEntity<?> assignCrew(@RequestHeader(name = "Authorization") String token,
			@Valid @RequestBody FlightAssignmentRequest assigRequest) {
		if (authorisationClient.validate(token)) {
			try {
				CrewAssignment assignment = flightAssignment.assignCrewToFlight(assigRequest.getCrewId(),
						assigRequest.getFlightId(), assigRequest.getClassType());
				return new ResponseEntity<>(assignment,HttpStatus.CREATED);
			} catch (CrewConflictException | MaxWorkingHourExceedException | IllegalArgumentException e) {
				return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
			}
		}
		else {
			return new ResponseEntity<>("Authontication Required", HttpStatus.FORBIDDEN);
		}
	}
}
