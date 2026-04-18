package com.ksv.tactive.project.dao;

import com.ksv.tactive.dto.TenderEstimationTreeDto;
import com.ksv.tactive.project.entity.InternalBoq;
import com.ksv.tactive.project.entity.Projects;
import com.ksv.tactive.project.entity.Resources;
import com.ksv.tactive.project.entity.Uom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InternalBoqDao extends JpaRepository<InternalBoq, UUID> {
    List<InternalBoq> findByProjectAndResourceAndUom(Projects project, Resources resources, Uom uom);
    List<InternalBoq> findByProject_Id(UUID uuid);
    List<InternalBoq> findByProject(Projects project);
}
