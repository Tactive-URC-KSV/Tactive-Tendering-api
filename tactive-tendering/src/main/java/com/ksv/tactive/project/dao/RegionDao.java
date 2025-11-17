package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.Region;
@Repository
public interface RegionDao extends JpaRepository<Region, String>{
	Region findByRegionName(String regionName);
	Region findByRegionNameIgnoreCase(String regionName);
}
