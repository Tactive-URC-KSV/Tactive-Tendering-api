package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.ProjectStatus;
@Repository
public interface ProjectStatusDao extends JpaRepository<ProjectStatus, String> {
	ProjectStatus findByProcessUrl(String url);
	ProjectStatus findByStatus(String status);
	ProjectStatus findByStatusIgnoreCase(String status);
}
