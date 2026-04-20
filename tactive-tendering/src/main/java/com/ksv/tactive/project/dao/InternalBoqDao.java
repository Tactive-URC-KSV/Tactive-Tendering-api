package com.ksv.tactive.project.dao;

import com.ksv.tactive.dto.TenderEstimationTreeDto;
import com.ksv.tactive.project.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InternalBoqDao extends JpaRepository<InternalBoq, UUID> {
    List<InternalBoq> findByProject(Projects project);
    List<InternalBoq> findByProject_IdAndResource_IdAndUom_Id(UUID id, UUID id1, UUID id2);
}
