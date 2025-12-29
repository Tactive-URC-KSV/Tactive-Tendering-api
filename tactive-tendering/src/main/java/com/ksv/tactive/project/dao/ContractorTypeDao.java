package com.ksv.tactive.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.ContractorType;

@Repository
public interface ContractorTypeDao extends JpaRepository<ContractorType, String>{
    boolean existsByTypeIgnoreCase(String type);
}
