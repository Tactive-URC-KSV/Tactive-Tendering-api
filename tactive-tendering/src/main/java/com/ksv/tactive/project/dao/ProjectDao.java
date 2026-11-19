package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.Projects;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectDao extends JpaRepository<Projects, UUID> {
    List<Projects> findByCompanyIdAndArchiveFalse(String companyId);
    List<Projects> findByArchiveFalse();
}
