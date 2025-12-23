package com.ksv.tactive.contractor.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.contractor.entity.ContractorGrade;

@Repository
public interface ContractorGradeDao extends JpaRepository<ContractorGrade, String>{
    boolean existsByGradeNameIgnoreCase(String gradeName);
}
