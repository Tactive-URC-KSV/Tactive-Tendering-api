package com.ksv.tactive.project.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.TenderEstimation;

@Repository
public interface TenderEstimationDao extends JpaRepository<TenderEstimation, String>{

	List<TenderEstimation> findByCostCodeActivityId(String costCodeId);

}
