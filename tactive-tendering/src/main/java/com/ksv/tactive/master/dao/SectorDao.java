package com.ksv.tactive.master.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.master.entity.Sector;
@Repository
public interface SectorDao extends JpaRepository<Sector, String>{
	Sector findBySectorNameIgnoreCase(String sectorName);
}
