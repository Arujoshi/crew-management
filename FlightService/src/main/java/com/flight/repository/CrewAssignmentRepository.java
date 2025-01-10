package com.flight.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.flight.model.CrewAssignment;

@Repository
public interface CrewAssignmentRepository extends JpaRepository<CrewAssignment, Long> {

	List<CrewAssignment> findByFlightId(Long flightId);
}
