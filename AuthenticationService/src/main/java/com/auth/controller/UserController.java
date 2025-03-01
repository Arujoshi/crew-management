package com.auth.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.auth.model.AuthRequest;
import com.auth.model.UserInfo;
import com.auth.service.JwtService;
import com.auth.service.UserInfoService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private UserInfoService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;


    @PostMapping("/addNewUser")
    public String addNewUser(@Valid @RequestBody UserInfo userInfo) {
        return service.addUser(userInfo);
    }

    @GetMapping("/user/userProfile")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String userProfile() {
        return "Welcome to User Profile";
    }

    @GetMapping("/admin/adminProfile")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String adminProfile() {
        return "Welcome to Admin Profile";
    }
    
    @GetMapping("/accessDenied")
    public String unauthorized() {
    	return "Access denied";
    }

//    @PostMapping("/generateToken")
//    public String authenticateAndGetToken(@Valid @RequestBody AuthRequest authRequest) {
//        Authentication authentication = authenticationManager.authenticate(
//            new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword())
//        );
//        if (authentication.isAuthenticated()) {
//            return jwtService.generateToken(authRequest.getUserName());
//        } else {
//            throw new UsernameNotFoundException("Invalid user request!");
//        }
//    }
    
    @PostMapping("/generateToken")
    public ResponseEntity<Object> authenticateAndRedirect(@Valid @RequestBody AuthRequest authRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword())
        );

        if (authentication.isAuthenticated()) {
            String jwtToken = jwtService.generateToken(authRequest.getUserName());

            // Check the user's roles and determine redirection
            String redirectUrl;
            if (authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
                redirectUrl = "/auth/admin/adminProfile";
            } else if (authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER"))) {
                redirectUrl = "/auth/user/userProfile";
            } else {
                redirectUrl = "/auth/accessDenied"; // Fallback for no matching roles
            }

            // Set JWT token in the response header
            response.setHeader("Authorization", "Bearer " + jwtToken);

            // Return redirection URL as a response
            return ResponseEntity.ok(Map.of(
                "message", "Authentication successful!",
                "redirectUrl", redirectUrl
            ));
        } else {
            throw new UsernameNotFoundException("Invalid user request!");
        }
    }
    
    @GetMapping("/validate")
	public ResponseEntity<?> validate(@RequestHeader(name = "Authorization") String token1) {
		String token = token1.substring(7);
		try {
			UserDetails user = service.loadUserByUsername(jwtService.extractUsername(token));
			if (jwtService.validateToken(token, user)) {
				System.out.println("=================Inside Validate==================");
				return new ResponseEntity<>(true, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(false, HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(false, HttpStatus.FORBIDDEN);
		}
	}

}