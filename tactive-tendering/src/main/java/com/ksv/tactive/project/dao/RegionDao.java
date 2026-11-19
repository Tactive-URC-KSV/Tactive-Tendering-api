package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.Region;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegionDao extends JpaRepository<Region, UUID>{
	Region findByRegionName(String regionName);
	Region findByRegionNameIgnoreCase(String regionName);
    Optional<Region> findByRegionNameIgnoreCaseAndActiveTrue(String regionName);
}
