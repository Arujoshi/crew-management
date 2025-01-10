package com.flight.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.flight.dto.CrewMemberDto;

@FeignClient(name = "CrewService", url = "${CREWSERVICE:http://localhost:8001}")
public interface CrewMemberService {
	
    @GetMapping("/api/crew/{id}")
    public CrewMemberDto getCrewMemberById(@PathVariable("id") Long crewMemberId);
    
    @PutMapping("/api/crew/{crewId}")
    public CrewMemberDto updateCrew(@PathVariable("crewId") Long crewId, @RequestBody CrewMemberDto crewMemberDto);

}