package com.ksv.tactive.project.dao;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.CostCodeActivity;
@Repository
public interface CostCodeDao extends JpaRepository<CostCodeActivity, UUID>{
	List<CostCodeActivity> findByProjectId(UUID projectId);
	List<CostCodeActivity> findByProjectIdAndCostCodeTypeId(UUID projectId, UUID costCodeTypeId);
}
