package com.ksv.tactive.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.ContractorAddInfo;

@Repository
public interface ContractorAddInfoDao extends JpaRepository<ContractorAddInfo, String>{
    ContractorAddInfo findByContractorId(String id);
}
