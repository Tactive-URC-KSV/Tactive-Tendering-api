package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.BoqUomMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoqUomMappingDao extends JpaRepository<BoqUomMapping, UUID> {
    List<BoqUomMapping> findByProjectIdOrderByClientUomAsc(String projectId);

    Optional<BoqUomMapping> findByProjectIdAndClientUomIgnoreCase(String projectId, String clientUom);
}
