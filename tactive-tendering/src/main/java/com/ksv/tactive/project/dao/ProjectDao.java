package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.Projects;
@Repository
public interface ProjectDao extends JpaRepository<Projects, String> {
	Projects findByProjectCode(String projectCode);
	
}
