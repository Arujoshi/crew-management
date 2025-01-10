package com.crew.service;

import java.util.List;

import com.crew.model.CrewMember;

public interface CrewMemberService {
	abstract CrewMember getCrewMemberById(Long id);
	
	abstract List<CrewMember> getAllCrewMembers();
	
	abstract CrewMember addCrewMember(CrewMember member);
	
	abstract CrewMember deleteCrewMember(Long id);
	
	abstract CrewMember updateCrewMember(Long id, CrewMember member);
}

