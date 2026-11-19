package com.ksv.tactive.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.ContractorType;

import java.util.UUID;

@Repository
public interface ContractorTypeDao extends JpaRepository<ContractorType, UUID>{
    boolean existsByTypeIgnoreCase(String type);
}
