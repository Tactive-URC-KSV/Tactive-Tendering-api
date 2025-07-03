package com.ksv.tactive.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.entity.Projects;

@Repository
public interface ProjectDao extends JpaRepository<Projects, String> {
	
	Projects findByProjectCode(String projectCode);
	
}
