package com.ksv.tactive.project.dao;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.BOQ;
@Repository
public interface BOQDao extends JpaRepository<BOQ, UUID>{
	List<BOQ> findByProjectId(UUID projectId);
	List<BOQ> findByProjectIdAndParentBOQId(UUID projectId, UUID parentBoqId);
    List<BOQ> findByProjectIdAndBoqNameContainingIgnoreCase(UUID projectId, String searchQuery);
}
