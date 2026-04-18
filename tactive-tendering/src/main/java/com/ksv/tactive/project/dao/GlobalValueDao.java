package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.GlobalValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GlobalValueDao extends JpaRepository<GlobalValue, UUID> {
    List<GlobalValue> findByProject_Id(UUID projectId);
    boolean existsByProject_IdAndNameIgnoreCaseAndIdNot(UUID projId, String name, UUID id);
    boolean existsByProject_IdAndCodeIgnoreCaseAndIdNot(UUID projId, String code, UUID id);
    boolean existsByProject_IdAndCodeIgnoreCase(UUID projId, String code);
    boolean existsByProject_IdAndNameIgnoreCase(UUID projId, String name);
}
