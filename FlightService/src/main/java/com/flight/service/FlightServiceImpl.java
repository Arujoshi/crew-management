package com.flight.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flight.model.Flight;
import com.flight.repository.FlightRepository;
import com.flight.dto.FlightStatusUpdateRequest;
import com.flight.exception.FlightNotFoundException;

@Service
public class FlightServiceImpl implements FlightService {
	
	@Autowired
	private FlightRepository flightRepo;
	

	@Override
	public Flight getFlightById(Long id) {
		Optional<Flight> flightGot=flightRepo.findById(id);
		if(flightGot.isPresent()) {
			return flightGot.get();
		}else {
			throw new FlightNotFoundException("Flight not found with id "+id);
		}
	}

	@Override
	public List<Flight> getAllFlights() {
		return flightRepo.findAll();
	}

	@Override
	public Flight addFlight(Flight flight) {
		return flightRepo.save(flight);
	}

	@Override
	public Flight deleteFlight(Long id) {
		Optional<Flight> flightGot=flightRepo.findById(id);
		if(flightGot.isPresent()) {
			Flight flight=flightGot.get();
			flightRepo.deleteById(id);
			return flight;
		}else {
			throw new FlightNotFoundException("Flight not found with id "+id);
		}
	}

	@Override
	public Flight updateFlight(Long id, Flight flight) {
		Optional<Flight> flightGot=flightRepo.findById(id);
		if(flightGot.isPresent()) {
			Flight exsitingFlight=flightGot.get();
			exsitingFlight.setFlightNumber(flight.getFlightNumber());
			exsitingFlight.setFlightType(flight.getFlightType());
			exsitingFlight.setSource(flight.getSource());
			exsitingFlight.setDestination(flight.getDestination());
			exsitingFlight.setTurnaroundTime(flight.getTurnaroundTime());
			exsitingFlight.setDepartureTime(flight.getDepartureTime());
			exsitingFlight.setArrivalTime(flight.getArrivalTime());
			exsitingFlight.setActualDepartureTime(flight.getActualDepartureTime());
			exsitingFlight.setActualArrivalTime(flight.getActualArrivalTime());
			exsitingFlight.setFuelConsumption(flight.getFuelConsumption());
			exsitingFlight.setEnvironmentalImpact(flight.getEnvironmentalImpact());
			exsitingFlight.setAvailableClasses(flight.getAvailableClasses());
			exsitingFlight.setRequiredQualifications(flight.getRequiredQualifications());
			exsitingFlight.setOnTimePerformance(flight.getOnTimePerformance());
			exsitingFlight.setFlightStatus(flight.getFlightStatus());
			
			return flightRepo.save(exsitingFlight);
		}else {
			throw new FlightNotFoundException("Flight not found with id "+id);
		}
	}
}
