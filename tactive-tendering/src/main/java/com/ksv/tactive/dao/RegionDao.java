package com.ksv.tactive.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.entity.Region;

@Repository
public interface RegionDao extends JpaRepository<Region, String>{
	
	List<Region> findAllByOrderByCountryAsc();
}
