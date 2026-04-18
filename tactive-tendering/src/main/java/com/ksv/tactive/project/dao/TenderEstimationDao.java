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
    List<TenderEstimation> findByBoqInAndResourceNature(List<BOQ> boqList, ResourceNature resourceNature);
	List<TenderEstimation> findByProject_Id(UUID projectId);
}
