package com.flight.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Flights")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flightId;

    @NotBlank(message = "Flight number cannot be blank")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Invalid flight number format")
    @Column(name = "FlightNumber", unique = true, nullable = false)
    private String flightNumber;

    @NotBlank(message = "Flight Type cannot be blank")
    @Column(name = "FlightType", nullable = false)
    private String flightType; // e.g., Domestic, International, Cargo

    @NotBlank(message = "Source cannot be blank")
    @Column(name = "Source", nullable = false)
    private String source;

    @NotBlank(message = "Destination cannot be blank")
    @Column(name = "Destination", nullable = false)
    private String destination;
   
    @NotNull(message = "Turnaround time is required")
    @Min(value = 10, message = "Turnaround time must be at least 10 minutes")
    @Max(value = 1440, message = "Turnaround time cannot exceed 1440 minutes (1 day)")
    @Column(name = "turnaround_time_minutes")
    private Integer turnaroundTime; // Time in minutes between landing and next departure

    @Column(name = "DepartureTime", nullable = false)
    @NotNull(message = "Departure time is required")
    private LocalDateTime departureTime;
    
    @Column(name = "ActualDepartureTime", nullable=true)
    private LocalDateTime actualDepartureTime;

    @NotNull(message = "Arrival time is required")
    @Column(name = "ArrivalTime")
    private LocalDateTime arrivalTime;
    
    @Column(name = "ActualArrivalTime", nullable=true)
    private LocalDateTime actualArrivalTime;

    @Column(name = "FuelConsumption")
    @Positive(message = "Fuel consumption must be positive")
    private double fuelConsumption; // in liters per hour

    @NotBlank(message = "Environmental impact is required")
    @Pattern(regexp = "Low|Medium|High", message = "Environmental impact must be one of: Low, Medium, High")
    @Column(name = "EnvironmentalImpact")
    private String environmentalImpact; // e.g., Low, Medium, High

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "AvailableClasses", joinColumns = @JoinColumn(name = "FlightId"))
    @Column(name = "Class")
    @NotEmpty(message = "Available classes must contain at least one class")
    private List<String> availableClasses; // e.g., Economy, Business, First Class

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "RequiredQualifications", joinColumns = @JoinColumn(name = "FlightId"))
    @Column(name = "Qualification")
    @NotEmpty(message = "Required qualifications must contain at least one qualification")
    private List<String> requiredQualifications;

    @Column(name = "OnTimePerformance", nullable = false)
    @NotNull(message = "On-time performance is required")
    @Min(value = 0, message = "On-time performance cannot be less than 0%")
    @Max(value = 100, message = "On-time performance cannot be more than 100%")
    private double onTimePerformance; // percentage

    @Column(name = "FlightStatus", nullable = false)
    @NotBlank(message = "Flight status is required")
    private String flightStatus; // e.g., Scheduled, Boarding, In Flight, Delayed, Completed

}
