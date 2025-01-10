package com.flight.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CrewMemberDto {

	private Long crewId;
	private String crewName;
	private LocalDate dateOfBirth;
	private String gender;
	private String position;
	private String department;
	private String contactNo;
	private String emailId;
	private String address;
	private LocalDate dateOfJoining;
	private String employementStatus;
	private List<EmergencyContact> emergencyContacts;
	private List<String> languageSpoken;
	private List<String> qualifications;
	private LocalDateTime lastFlightEndTime;
	private int totalHoursWorked = 0;
	private LocalDate lastWorkedDate;
	private String availabilityStatus; // e.g., Available, On Duty, Resting, Sick
	private boolean isMedicallyCleared;
	private LocalDate lastMedicalCheckup;

}
