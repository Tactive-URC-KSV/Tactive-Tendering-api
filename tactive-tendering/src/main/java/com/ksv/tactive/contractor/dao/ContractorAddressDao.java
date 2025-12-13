package com.ksv.tactive.contractor.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.contractor.entity.ContractorAddress;

@Repository
public interface ContractorAddressDao extends JpaRepository<ContractorAddress, String>{

}
