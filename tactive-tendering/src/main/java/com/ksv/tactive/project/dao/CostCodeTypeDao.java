package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.CostCodeType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CostCodeTypeDao extends JpaRepository<CostCodeType, UUID>{
	CostCodeType findByCostCodeName(String costCodeName);
    Optional<CostCodeType> findByCostCodeNameIgnoreCaseAndActiveTrue(String costCodeName);
}
