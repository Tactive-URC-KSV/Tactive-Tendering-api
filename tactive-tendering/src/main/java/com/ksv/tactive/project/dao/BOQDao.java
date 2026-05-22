package com.ksv.tactive.project.dao;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.BOQ;
@Repository
public interface BOQDao extends JpaRepository<BOQ, UUID>{
	List<BOQ> findByProjectId(String projectId);
	List<BOQ> findByProjectIdAndParentBOQId(String projectId, UUID parentBoqId);
//    List<BOQ> findByProjectIdAndBoqNameContainingIgnoreCase(UUID projectId, String searchQuery);
    Optional<BOQ> findByProjectIdAndBoqCodeIgnoreCaseAndBoqNameIgnoreCase(String id, String boqCode, String boqName);
    Page<BOQ> findByProjectIdAndParentBOQIsNull(String projectId, Pageable pageable);
}
