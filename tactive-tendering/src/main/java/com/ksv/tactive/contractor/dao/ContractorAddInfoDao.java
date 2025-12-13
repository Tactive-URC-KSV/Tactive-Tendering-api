package com.ksv.tactive.contractor.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.contractor.entity.ContractorAddInfo;

@Repository
public interface ContractorAddInfoDao extends JpaRepository<ContractorAddInfo, String>{

}
