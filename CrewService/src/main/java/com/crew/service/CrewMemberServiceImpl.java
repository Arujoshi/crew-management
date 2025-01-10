package com.crew.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.crew.exception.CrewMemberNotFoundException;
import com.crew.model.CrewMember;
import com.crew.repository.CrewMemberRepository;


@Service
public class CrewMemberServiceImpl implements CrewMemberService {

	@Autowired
	private CrewMemberRepository crewRepository;
	
	@Override
	public CrewMember getCrewMemberById(Long id) {
		Optional<CrewMember> crew=crewRepository.findById(id);
		
		if(crew.isPresent()) {
			return crew.get();
		}
		else {
			throw new CrewMemberNotFoundException("Crew member not found with id: " + id);
		}
	}

	@Override
	public List<CrewMember> getAllCrewMembers() {
		return crewRepository.findAll();
	}

	@Override
	public CrewMember addCrewMember(CrewMember member) {
		return crewRepository.save(member);
	}

	@Override
	public CrewMember deleteCrewMember(Long id) {
		Optional<CrewMember> crew=crewRepository.findById(id);
		if(crew.isPresent()) {
			CrewMember crewFound=crew.get();
			crewRepository.deleteById(id);
			return crewFound;
		}
		else {
			throw new CrewMemberNotFoundException("Crew member not found with id: " + id);
		}
	}


	@Override
	public CrewMember updateCrewMember(Long id, CrewMember member) {
		CrewMember existingCrewMember = crewRepository.findById(id)
	            .orElseThrow(() -> new CrewMemberNotFoundException("Crew member not found with id: " + id));

	    // Update fields
	    existingCrewMember.setCrewName(member.getCrewName());
	    existingCrewMember.setDateOfBirth(member.getDateOfBirth());
	    existingCrewMember.setGender(member.getGender());
	    existingCrewMember.setPosition(member.getPosition());
	    existingCrewMember.setDepartment(member.getDepartment());
	    existingCrewMember.setContactNo(member.getContactNo());
	    existingCrewMember.setEmailId(member.getEmailId());
	    existingCrewMember.setAddress(member.getAddress());
	    existingCrewMember.setDateOfJoining(member.getDateOfJoining());
	    existingCrewMember.setEmployementStatus(member.getEmployementStatus());
	    existingCrewMember.setEmergencyContacts(member.getEmergencyContacts());
	    existingCrewMember.setLanguageSpoken(member.getLanguageSpoken());
	    existingCrewMember.setQualifications(member.getQualifications());
	    existingCrewMember.setLastFlightEndTime(member.getLastFlightEndTime());
	    existingCrewMember.setTotalHoursWorked(member.getTotalHoursWorked());
	    existingCrewMember.setLastWorkedDate(member.getLastWorkedDate());
	    existingCrewMember.setAvailabilityStatus(member.getAvailabilityStatus());
	    existingCrewMember.setLastMedicalCheckup(member.getLastMedicalCheckup());
	    existingCrewMember.setMedicallyCleared(member.isMedicallyCleared());

	    // Save updated entity
	    return crewRepository.save(existingCrewMember);
	}
	
	@Scheduled(cron = "0 0 0 * * *") // Runs at midnight
    public void resetTotalHoursWorked() {
        List<CrewMember> crewMembers = crewRepository.findAll();
        for (CrewMember crew : crewMembers) {
            if (crew.getTotalHoursWorked() > 0) {
                crew.setTotalHoursWorked(0);
                crew.setLastWorkedDate(LocalDate.now());
                crewRepository.save(crew);
            }
        }
    }

}
