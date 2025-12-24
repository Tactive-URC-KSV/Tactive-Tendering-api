package com.ksv.tactive.contractor.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.contractor.entity.ContractorNature;

@Repository
public interface ContractorNatureDao extends JpaRepository<ContractorNature, String>{
    boolean existsByNatureIgnoreCaseAndContractorType_Id(String nature, String id);
}
