package com.ksv.tactive.contractor.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.contractor.entity.ContractorType;

@Repository
public interface ContractorTypeDao extends JpaRepository<ContractorType, String>{

}
