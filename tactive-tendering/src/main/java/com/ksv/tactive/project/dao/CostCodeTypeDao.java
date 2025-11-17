package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.CostCodeType;
@Repository
public interface CostCodeTypeDao extends JpaRepository<CostCodeType, String>{
	CostCodeType findByCostCodeName(String costCodeName);
}
