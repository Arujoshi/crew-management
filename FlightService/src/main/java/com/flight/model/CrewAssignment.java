package com.flight.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CrewAssignment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Crew ID is required")
    @Positive(message = "Crew ID must be a positive number")
	private Long crewId;
	
	@NotNull(message = "Flight ID is required")
    @Positive(message = "Flight ID must be a positive number")
	private Long flightId;

	@NotBlank(message = "Class type is required")
    @Pattern(regexp = "Economy|Business|First Class", message = "Class type must be one of: Economy, Business, First Class")
	private String classType;

	@NotNull(message = "Assigned time is required")
    @FutureOrPresent(message = "Assigned time must be in the present or future")
	private LocalDateTime assignedTime;

}
