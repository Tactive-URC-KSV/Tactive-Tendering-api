package com.ksv.tactive.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.CostCodeActivity;

@Repository
public interface CostCodeDao extends JpaRepository<CostCodeActivity, String>{

}
