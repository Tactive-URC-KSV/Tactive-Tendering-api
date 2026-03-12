package com.ksv.tactive.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.ContractorTaxDetails;

import java.util.UUID;

@Repository
public interface ContractorTaxDetailsDao extends JpaRepository<ContractorTaxDetails, UUID> {
    ContractorTaxDetails findByContractorId(UUID id);
}
