package com.flight.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FlightAssignmentRequest {

	private Long crewId;
	
	private Long flightId;
	
	private String classType;
}
