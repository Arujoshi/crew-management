package com.crew.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crew.feign.AuthorisationClient;
import com.crew.model.CrewMember;
import com.crew.service.CrewMemberServiceImpl;

import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/crew")
public class CrewMemberController {

	@Autowired
	private CrewMemberServiceImpl crewService;

	@Autowired
	private AuthorisationClient authorisationClient;

	@GetMapping
	public ResponseEntity<?> getAllCrewMembers(@RequestHeader(name = "Authorization") String token) {
		List<CrewMember> crew = crewService.getAllCrewMembers();
		if (authorisationClient.validate(token)) {
			return new ResponseEntity<>(crew, HttpStatus.OK);

		} else {
			return new ResponseEntity<>("Authontication Required", HttpStatus.FORBIDDEN);
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getCrewMemberById(@RequestHeader(name = "Authorization") String token,
			@PathVariable("id") Long id) {
		if (authorisationClient.validate(token)) {
			CrewMember crewMember = crewService.getCrewMemberById(id);
			return new ResponseEntity<>(crewMember, HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Authontication Required", HttpStatus.FORBIDDEN);
		}
	}

	@PostMapping
	public ResponseEntity<?> addCrewMember(@RequestHeader(name = "Authorization") String token,
			@Valid @RequestBody CrewMember crewMember) {
		if (authorisationClient.validate(token)) {
			return new ResponseEntity<>(crewService.addCrewMember(crewMember), HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>("Authontication Required", HttpStatus.FORBIDDEN);
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteCrewMember(@RequestHeader(name = "Authorization") String token,
			@PathVariable("id") Long id) {
		if (authorisationClient.validate(token)) {
			return new ResponseEntity<>(crewService.deleteCrewMember(id), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Authontication Required", HttpStatus.FORBIDDEN);
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateCrew(@RequestHeader(name = "Authorization")String token,@PathVariable("id") Long id,@Valid @RequestBody CrewMember updatedCrewMember) {
		if (authorisationClient.validate(token)) {
			CrewMember updatedCrew = crewService.updateCrewMember(id, updatedCrewMember);
			return new ResponseEntity<>(updatedCrew, HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>("Authontication Required", HttpStatus.FORBIDDEN);
		}
	}

}
