package com.ksv.tactive.project.dao;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.TenderEstimationCostDetails;
@Repository
public interface TenderEstCostDao extends JpaRepository<TenderEstimationCostDetails, String>{

	@Query("SELECT c FROM TenderEstimationCostDetails c WHERE c.tenderEstimation.id = :tenderEstimationId AND c.boq.id = :boqId")
    Optional<TenderEstimationCostDetails> findByTenderEstimationIdAndBoqId(
        @Param("tenderEstimationId") String tenderEstimationId, 
        @Param("boqId") String boqId);
    
    @Query("SELECT c FROM TenderEstimationCostDetails c WHERE c.tenderEstimation.id = :tenderEstimationId")
    List<TenderEstimationCostDetails> findByTenderEstimationId(@Param("tenderEstimationId") String tenderEstimationId);
	
}
