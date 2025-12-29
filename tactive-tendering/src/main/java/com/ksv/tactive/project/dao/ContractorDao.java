package com.ksv.tactive.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.Contractor;

@Repository
public interface ContractorDao extends JpaRepository<Contractor, String>{

}
