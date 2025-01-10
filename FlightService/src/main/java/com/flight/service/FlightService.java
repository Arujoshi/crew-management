package com.flight.service;

import java.util.List;

import com.flight.dto.FlightStatusUpdateRequest;
import com.flight.model.Flight;

public interface FlightService {

	abstract Flight getFlightById(Long id);
	
	abstract List<Flight> getAllFlights();
	
	abstract Flight addFlight(Flight flight);
	
	abstract Flight deleteFlight(Long id);
	
	abstract Flight updateFlight(Long id,Flight flight);
	
}

