package com.ksv.tactive.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.ContractorNature;

import java.util.List;

@Repository
public interface ContractorNatureDao extends JpaRepository<ContractorNature, String>{
    boolean existsByNatureIgnoreCaseAndContractorType_Id(String nature, String id);
    List<ContractorNature> findByContractorTypeId(String contractorTypeId);
}
