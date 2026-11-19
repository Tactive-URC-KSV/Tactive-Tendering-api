package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.SectorFields;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SectorFieldsDao extends JpaRepository<SectorFields, UUID>{
    List<SectorFields> findAllBySector_Id(UUID sectorId);
    boolean existsByFieldNameAndSector_IdAndIdNot(String fieldName, UUID sectorId, UUID id);
    boolean existsByFieldNameAndSector_Id(String fieldName, UUID sectorId);
}
