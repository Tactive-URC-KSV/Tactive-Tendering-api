package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DesignationDao extends JpaRepository<Designation, UUID> {
    boolean existsByDesignationNameIgnoreCaseAndIdNot(String designationName, UUID id);
    boolean existsByDesignationNameIgnoreCase(String designationName);
}
