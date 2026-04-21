package com.ksv.tactive.project.dao;
import java.util.List;
import java.util.UUID;

import com.ksv.tactive.project.entity.BOQ;
import com.ksv.tactive.util.ResourceNature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.TenderEstimation;
@Repository
public interface TenderEstimationDao extends JpaRepository<TenderEstimation, UUID>{
	List<TenderEstimation> findByBoqId(UUID boqId);
	List<TenderEstimation> findByParent_Id(UUID parentId);
    List<TenderEstimation> findByBoq_IdAndProject_IdAndResources_Id(UUID boqId, UUID projectId, UUID resourceId);

    List<TenderEstimation> findByParentInternalBoq_Id(UUID internalBoqId);
    List<TenderEstimation> findByParentInternalBoq_IdAndResources_IdAndUom_Id(UUID internalBoqId, UUID resourceId, UUID uomId);
    List<TenderEstimation> findByBoq_IdAndParentIsNullAndResources_IdAndUom_Id(UUID id, UUID resourceId, UUID uomId);
}
