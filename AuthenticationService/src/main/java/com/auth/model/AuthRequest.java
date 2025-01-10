package com.auth.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {

	@NotBlank(message = "Username is required")
	private String userName;
	
	@NotBlank(message = "Password is required")
    @Size(min = 10, message = "Password must be between 10 and 20 characters")
	private String password; 
}
