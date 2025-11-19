package com.ksv.tactive.project.dao;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.TenderEstimationCostDetails;
@Repository
public interface TenderEstCostDao extends JpaRepository<TenderEstimationCostDetails, String>{

	Optional<TenderEstimationCostDetails> findByTenderEstimationIdAndBoqId(String id, String boqId);
	
}
