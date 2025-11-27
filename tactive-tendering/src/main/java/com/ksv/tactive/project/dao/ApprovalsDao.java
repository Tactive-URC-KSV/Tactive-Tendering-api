package com.ksv.tactive.project.dao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.ApprovalDocuments;
@Repository
public interface ApprovalsDao extends JpaRepository<ApprovalDocuments, String>{
	List<ApprovalDocuments> findAllByTechnicalFeasibilityId(String techId);
}
