package com.flight.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.flight.model.Flight;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
	Optional<Flight> findByFlightNumber(String flightNumber);
	
	@Query("SELECT COUNT(f) FROM Flight f WHERE f.flightNumber LIKE :prefix%")
	int countByFlightNumberStartingWith(@Param("prefix") String prefix);

}
