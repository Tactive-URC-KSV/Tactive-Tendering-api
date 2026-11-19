package com.ksv.tactive.project.dao;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.ApprovalDocuments;
@Repository
public interface ApprovalsDao extends JpaRepository<ApprovalDocuments, UUID>{
	List<ApprovalDocuments> findAllByTechnicalFeasibilityId(UUID techId);
}
