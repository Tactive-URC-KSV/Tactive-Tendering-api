package com.ksv.tactive.master.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.master.entity.Region;

import java.util.Optional;

@Repository
public interface RegionDao extends JpaRepository<Region, String>{
	Region findByRegionName(String regionName);
	Region findByRegionNameIgnoreCase(String regionName);
    Optional<Region> findByRegionNameIgnoreCaseAndActiveTrue(String regionName);
}
