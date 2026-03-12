package com.ksv.tactive.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.ContractorGrade;

import java.util.UUID;

@Repository
public interface ContractorGradeDao extends JpaRepository<ContractorGrade, UUID>{
    boolean existsByGradeNameIgnoreCase(String gradeName);
}
