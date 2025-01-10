package com.flight.service;

import java.util.ArrayList;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.flight.dto.CrewMemberDto;
import com.flight.exception.CrewConflictException;
import com.flight.exception.FlightNotFoundException;
import com.flight.exception.InsufficientRestTimeException;
import com.flight.exception.MaxWorkingHourExceedException;
import com.flight.feign.CrewMemberService;
import com.flight.model.CrewAssignment;
import com.flight.model.Flight;
import com.flight.repository.CrewAssignmentRepository;
import com.flight.repository.FlightRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class FlightAssignmentService {

	@Autowired
	CrewMemberService crewService;

	@Autowired
	private FlightRepository flightRepo;

	@Autowired
	private CrewAssignmentRepository crewAssignRepo;

	@Autowired
	private RouteServiceImpl routeService;


	final int maxWorkingHours = 12;

	public CrewAssignment assignCrewToFlight(Long crewId, Long flightId, String classType) {
		// Fetch Crew Member Details
		CrewMemberDto crewMember = crewService.getCrewMemberById(crewId);

		// Fetch Flight Details
		Flight flight = flightRepo.findById(flightId)
				.orElseThrow(() -> new FlightNotFoundException("Flight not found with id " + flightId));

		// 1. Validate if the class type is available for the flight
		validateClassType(flight, classType);

		// 2. Validate flight status
		validateFlightStatus(flight);

		// 3. Validate crew availability
		validateCrewAvailability(crewMember);

		// 4. Check crew qualifications for the flight
		validateQualifications(crewMember, flight);

		// 6. Calculate flight duration
		long flightDuration = calculateFlightDuration(flight);

		// 7. Validate total working hours for the crew
		validateWorkingHours(crewMember, flightDuration);

		// 8. Ensure 2-hour break between previous flight and next flight
		validateBreakBetweenFlights(crewMember, flight);

		// Assign Crew to Flight
		CrewAssignment assignment = createCrewAssignment(crewId, flightId, classType, flightDuration);

		// Update CrewMember Availability and Working Hours
		updateCrewMemberDetails(crewMember, flight, flightDuration);

		// Save Crew Assignment
		return crewAssignRepo.save(assignment);
	}

	private void validateClassType(Flight flight, String classType) {
		if (!flight.getAvailableClasses().contains(classType)) {
			throw new IllegalArgumentException("Invalid class type for the flight");
		}
	}

	private void validateFlightStatus(Flight flight) {
		if (flight.getFlightStatus().contains("Departed") || flight.getFlightStatus().contains("In Air")) {
			throw new IllegalStateException("Cannot assign crew to a flight that is in air or already landed.");
		}
	}

	private void validateCrewAvailability(CrewMemberDto crewMember) {
		if (!"Available".equalsIgnoreCase(crewMember.getAvailabilityStatus())) {
			throw new CrewConflictException("Crew member is not available for assignment");
		}
	}

	private void validateQualifications(CrewMemberDto crewMember, Flight flight) {
		boolean hasRequiredQualification = crewMember.getQualifications().stream()
				.anyMatch(flight.getRequiredQualifications()::contains);

		if (!hasRequiredQualification) {
			throw new IllegalArgumentException("Crew member lacks required qualifications for the flight");
		}
	}

	private void validateWorkingHours(CrewMemberDto crewMember, long flightDuration) {
		if (crewMember.getTotalHoursWorked() + flightDuration > 12) {
			throw new MaxWorkingHourExceedException("Cannot assign crew. Daily working hours exceeded.");
		}
	}

	private void validateBreakBetweenFlights(CrewMemberDto crewMember, Flight nextFlight) {
		if (crewMember.getLastFlightEndTime() != null) {
			LocalDateTime lastFlightEndTime = crewMember.getLastFlightEndTime();
			LocalDateTime nextFlightDepartureTime = nextFlight.getDepartureTime();

			long breakDuration = Duration.between(lastFlightEndTime, nextFlightDepartureTime).toHours();

			if (breakDuration < 2) {
				throw new InsufficientRestTimeException(
						"Insufficient break time between previous flight and next flight (minimum 2 hours required).");
			}
		}
	}

	private long calculateFlightDuration(Flight flight) {
		return Duration.between(flight.getDepartureTime(), flight.getArrivalTime()).toHours();
	}

	private CrewAssignment createCrewAssignment(Long crewId, Long flightId, String classType, long flightDuration) {
		CrewAssignment assignment = new CrewAssignment();
		assignment.setFlightId(flightId);
		assignment.setCrewId(crewId);
		assignment.setClassType(classType);
		assignment.setAssignedTime(LocalDateTime.now());
		return assignment;
	}

	private void updateCrewMemberDetails(CrewMemberDto crewMember, Flight flight, long flightDuration) {
		crewMember.setAvailabilityStatus("On Duty");
		crewMember.setTotalHoursWorked(crewMember.getTotalHoursWorked() + (int) flightDuration);
		crewMember.setLastWorkedDate(LocalDate.now());
		crewMember.setLastFlightEndTime(flight.getArrivalTime());
		crewService.updateCrew(crewMember.getCrewId(), crewMember);
	}

	@Transactional
	public void updateFlightStatus(Long flightId) {
	    Flight flight = flightRepo.findById(flightId)
	            .orElseThrow(() -> new FlightNotFoundException("Flight not found with id "+flightId));
	    
	    LocalDateTime currentTime = LocalDateTime.now();
	    LocalDateTime scheduledDeparture = flight.getDepartureTime();
	    LocalDateTime scheduledArrival = flight.getArrivalTime();
	    LocalDateTime actualDeparture = flight.getActualDepartureTime();
	    LocalDateTime actualArrival = flight.getActualArrivalTime();
	    

	    // Scheduled
	    if (scheduledDeparture.isAfter(currentTime)) {
	        flight.setFlightStatus("Scheduled");
	    }
	    
	    // Delayed Departure
	    else if (scheduledDeparture.isBefore(currentTime) && actualDeparture == null) {
	        Duration delay = Duration.between(scheduledDeparture, currentTime);
	        long minutes = delay.toMinutes();
	        flight.setFlightStatus("Delayed by " + minutes + " minutes");
	    }
	    
	    // Departure
	    else if (actualDeparture != null && actualArrival == null && currentTime.isBefore(scheduledArrival)) {
	        Duration delayOrEarly = Duration.between(scheduledDeparture, actualDeparture);
	        long minutes = Math.abs(delayOrEarly.toMinutes());
	        if (actualDeparture.isBefore(scheduledDeparture)) {
	            flight.setFlightStatus("Departed Early by " + minutes + " minutes");
	        } else if (actualDeparture.isAfter(scheduledDeparture)) {
	            flight.setFlightStatus("Departed Late by " + minutes + " minutes");
	        }
	        if (actualDeparture != null && Duration.between(actualDeparture, currentTime).toMinutes() >= 30) {
	            flight.setFlightStatus("In Air");
	        }
	    }
	    
	    // Landed
	    else if (actualArrival != null) {
	        Duration delayOrEarly = Duration.between(scheduledArrival, actualArrival);
	        long minutes = Math.abs(delayOrEarly.toMinutes());
	        if (actualArrival.isBefore(scheduledArrival)) {
	            flight.setFlightStatus("Landed Early by " + minutes + " minutes");
	        } else if (actualArrival.isAfter(scheduledArrival)) {
	            flight.setFlightStatus("Landed Late by " + minutes + " minutes");
	        } else {
	            flight.setFlightStatus("Landed On Time");
	        }
	        scheduleNextFlight(flight);
	    }
         
	    flightRepo.save(flight);
	}

	@Scheduled(cron = "0 * * * * *") // This cron expression means "every minute"
	public void updateFlightStatusAutomatically() {
		// Fetch all flights from the database
		List<Flight> flights = flightRepo.findAll();

		// Loop through each flight and update its status dynamically
		for (Flight flight : flights) {
			if (!flight.getFlightStatus().contains("Landed")) {
	            updateFlightStatus(flight.getFlightId());
	        }
		}
	}

	public void updateCrewAvailability(Flight flight, boolean isFlightCompleted) {
		// Get the list of crew members assigned to the flight
		List<CrewAssignment> crewAssignments = crewAssignRepo.findByFlightId(flight.getFlightId());

		// Iterate through each crew member assigned to the flight
		for (CrewAssignment assignment : crewAssignments) {
			CrewMemberDto crewMember = crewService.getCrewMemberById(assignment.getCrewId());

			if (isFlightCompleted) {
				// If the flight is completed (landed or arrived), set the crew member's
				// availability to "Available"
				crewMember.setAvailabilityStatus("Available");
			} else {
				// If the flight is still in progress or being assigned, set the crew member's
				// status to "On Duty"
				crewMember.setAvailabilityStatus("On Duty");
			}

			// Update crew member availability status
			crewService.updateCrew(crewMember.getCrewId(), crewMember);
		}
	}

	@Transactional
	public void scheduleNextFlight(Flight landedFlight) {
		String nextDestination = routeService.getNextDestination(landedFlight.getDestination());
		int travelDuration = routeService.calculateFlightDuration(landedFlight.getDestination(), nextDestination);

		String newFlightNumber = generateFlightNumber(landedFlight.getFlightNumber());
		
		LocalDateTime nextDepartureTime = landedFlight.getActualArrivalTime()
				.plusMinutes(landedFlight.getTurnaroundTime());
		LocalDateTime nextArrivalTime = nextDepartureTime.plusMinutes(travelDuration);

		Flight newFlight = new Flight();
		newFlight.setFlightNumber(newFlightNumber);
		newFlight.setSource(landedFlight.getDestination());
		newFlight.setDestination(nextDestination);
		newFlight.setDepartureTime(nextDepartureTime);
		newFlight.setArrivalTime(nextArrivalTime);
		newFlight.setFlightStatus("Scheduled");
		newFlight.setFlightType(landedFlight.getFlightType());
		newFlight.setTurnaroundTime(landedFlight.getTurnaroundTime());
		newFlight.setFuelConsumption(landedFlight.getFuelConsumption());
		newFlight.setEnvironmentalImpact(landedFlight.getEnvironmentalImpact());
		newFlight.setOnTimePerformance(landedFlight.getOnTimePerformance());
		
		List<String> copiedAvailableClasses = new ArrayList<>(landedFlight.getAvailableClasses());
	    newFlight.setAvailableClasses(copiedAvailableClasses);

	    // Deep copy requiredQualifications
	    List<String> copiedRequiredQualifications = new ArrayList<>(landedFlight.getRequiredQualifications());
	    newFlight.setRequiredQualifications(copiedRequiredQualifications);


		flightRepo.save(newFlight);
	}

	public String generateFlightNumber(String baseFlightNumber) {
	    // Ensure that baseFlightNumber does not already include date or count
	    if (baseFlightNumber.contains("-")) {
	        baseFlightNumber = baseFlightNumber.split("-")[0];
	    }

	    // Format the current date as YYYYMMDD
	    String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

	    // Fetch flights from the database to determine the count
	    int count = flightRepo.countByFlightNumberStartingWith(baseFlightNumber + "-" + currentDate) + 1;

	    // Concatenate base flight number, date, and counter
	    return baseFlightNumber + "-" + currentDate + "-" + count;
	}
}
