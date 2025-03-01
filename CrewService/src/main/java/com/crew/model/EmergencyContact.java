package com.crew.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmergencyContact {
	
	@NotBlank(message = "Name is required")
    private String name;
	
	@NotBlank(message = "Relationship is required")
    private String relationship;
    
    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "\\d{10}", message = "Contact number must be a valid 10-digit number")
    private String contactNo;
    
}