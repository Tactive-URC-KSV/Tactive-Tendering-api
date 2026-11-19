package com.ksv.tactive.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.ContractorContacts;

import java.util.UUID;

@Repository
public interface ContractorContactsDao extends JpaRepository<ContractorContacts, UUID>{
    ContractorContacts findByContractorId(UUID id);
}
