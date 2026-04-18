package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.ProjectStatus;

import java.util.UUID;

@Repository
public interface ProjectStatusDao extends JpaRepository<ProjectStatus, UUID> {
	ProjectStatus findByProcessUrl(String url);
	ProjectStatus findByStatusIgnoreCase(String status);
}
