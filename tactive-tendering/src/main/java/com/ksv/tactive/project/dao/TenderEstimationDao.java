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
    List<TenderEstimation> findByProject_IdAndResources_IdAndUom_Id(UUID projectId, UUID resourcesId, UUID uomId);
    List<TenderEstimation> findByParent_IdAndResources_IdAndUom_Id(UUID parentId, UUID resourcesId, UUID uomId);
    List<TenderEstimation> findByParentIsNullAndResources_IdAndUom_Id(UUID resourcesId, UUID uomId);

    @org.springframework.data.jpa.repository.Query("SELECT te FROM TenderEstimation te LEFT JOIN FETCH te.estimationAttributes " +
            "WHERE te.parent.id = :parentId AND te.resources.id = :resourceId AND te.uom.id = :uomId")
    List<TenderEstimation> findDuplicatesWithAttributes(@org.springframework.data.repository.query.Param("parentId") UUID parentId,
                                                       @org.springframework.data.repository.query.Param("resourceId") UUID resourceId,
                                                       @org.springframework.data.repository.query.Param("uomId") UUID uomId);

    @org.springframework.data.jpa.repository.Query("SELECT te FROM TenderEstimation te LEFT JOIN FETCH te.estimationAttributes " +
            "WHERE te.parent IS NULL AND te.resources.id = :resourceId AND te.uom.id = :uomId")
    List<TenderEstimation> findRootDuplicatesWithAttributes(@org.springframework.data.repository.query.Param("resourceId") UUID resourceId,
                                                           @org.springframework.data.repository.query.Param("uomId") UUID uomId);

    List<TenderEstimation> findByBoq_IdAndParentIsNullAndResources_IdAndUom_Id(UUID boqId, UUID resourceId, UUID uomId);
}
