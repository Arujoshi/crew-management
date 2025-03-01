package com.crew.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrewMember {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long crewId;
	
	@Column(name="FullName")
	@NotBlank(message = "Crew name is required")
    @Size(max = 100, message = "Crew name must not exceed 100 characters")
	private String crewName;
	
	@Column(name="DateOfBirth")
	@NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
	private LocalDate dateOfBirth;
	
	@Column(name="Gender")
	@NotBlank(message = "Gender is required")
    @Pattern(regexp = "Male|Female|Transgender", message = "Gender must be Male, Female, or Other")
	private String gender;
	
	@NotBlank(message = "Position is required")
    @Size(max = 50, message = "Position must not exceed 50 characters")
	@Column(name="Position")
	private String position;
	
	@NotBlank(message = "Department is required")
    @Size(max = 50, message = "Department must not exceed 50 characters")
	@Column(name="Department")
	private String department;
	
	@NotBlank(message = "Contact number is required")
    @Pattern(regexp = "\\d{10}", message = "Contact number must be a valid 10-digit number")
	@Column(name="MobileNo")
	private String contactNo;
	
	@NotBlank(message = "Email ID is required")
    @Email(message = "Email ID must be a valid email address")
	@Column(name="EmailId")
	private String emailId;
	
	@Column(name="Address")
	@NotBlank(message = "Address is required")
    @Size(max = 200, message = "Address must not exceed 200 characters")
	private String address;
	
	@Column(name="JoiningDate")
	@NotNull(message = "Joining date is required")
    @PastOrPresent(message = "Joining date must be in the past or present")
	private LocalDate dateOfJoining;
	
	@Column(name="EmployementStatus")
	@NotBlank(message = "Employment status is required")
    @Pattern(regexp = "Active|Inactive|Retired|Suspended", message = "Employment status must be one of: Active, Inactive, Retired, Suspended")
	private String employementStatus;
	
	@ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "EmergencyContacts", joinColumns = @JoinColumn(name = "CrewId"))
	@Column(name="EmergencyContacts")
	@NotNull(message = "Emergency contacts are required")
    @Size(min = 1, message = "At least one emergency contact must be provided")
	private List<@Valid EmergencyContact> emergencyContacts;
	
	@ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "LanguageSpoken", joinColumns = @JoinColumn(name = "CrewId"))
	@Column(name="LanguageSpoken")
	@NotNull(message = "Languages spoken are required")
    @Size(min = 1, message = "At least one language must be provided")
	private List<String> languageSpoken;
	
	@ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "Qualifications", joinColumns = @JoinColumn(name = "CrewId"))
	@Column(name="Qualifications")
	@NotNull(message = "Qualifications are required")
    @Size(min = 1, message = "At least one qualification must be provided")
	private List<String> qualifications;
	
	@Column(name = "LastFlightEndTime")
	@PastOrPresent(message = "Last flight end time must be in the past or present")
    private LocalDateTime lastFlightEndTime;

    @Column(name = "TotalHoursWorked", nullable = false)
    @NotNull(message = "Total hours worked is required")
    @PositiveOrZero(message = "Total hours worked must be zero or a positive number")
    private int totalHoursWorked = 0;
    
    @Column(name = "LastWorkedDate")
    @PastOrPresent(message = "Last worked date must be in the past or present")
    private LocalDate lastWorkedDate;

    @Column(name = "AvailabilityStatus", nullable = false)
    @NotBlank(message = "Availability status is required")
    @Pattern(regexp = "Available|On Duty|Resting|Sick", message = "Availability status must be one of: Available, On Duty, Resting, Sick")
    private String availabilityStatus; // e.g., Available, On Duty, Resting, Sick

    @Column(name = "IsMedicallyCleared", nullable = false)
    @NotNull(message = "Medical clearance status is required")
    private boolean isMedicallyCleared;

    @Column(name = "LastMedicalCheckup")
    @Past(message = "Last medical checkup must be in the past")
    private LocalDate lastMedicalCheckup;
	
}
