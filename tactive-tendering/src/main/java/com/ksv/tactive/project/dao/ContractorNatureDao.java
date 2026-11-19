package com.ksv.tactive.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.ContractorNature;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractorNatureDao extends JpaRepository<ContractorNature, UUID>{
    boolean existsByNatureIgnoreCaseAndContractorType_Id(String nature, UUID id);
    List<ContractorNature> findByContractorTypeId(UUID contractorTypeId);
}
