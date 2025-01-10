package com.flight.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FlightStatusUpdateRequest {
    private LocalDateTime actualDepartureTime;
    private LocalDateTime actualArrivalTime;
    private String flightStatus; // e.g., Delayed, In Flight, Landed
}