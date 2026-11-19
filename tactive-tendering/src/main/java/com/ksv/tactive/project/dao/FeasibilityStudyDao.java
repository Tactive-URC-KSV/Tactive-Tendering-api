package com.ksv.tactive.project.dao;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.FeasibilityStudy;
@Repository
public interface FeasibilityStudyDao extends JpaRepository<FeasibilityStudy, UUID>{
	Optional<FeasibilityStudy> findByProjectId(UUID projectId);
}
