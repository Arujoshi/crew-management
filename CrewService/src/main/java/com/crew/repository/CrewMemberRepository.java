package com.crew.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.crew.model.CrewMember;

@Repository
public interface CrewMemberRepository extends JpaRepository<CrewMember, Long> {

}
