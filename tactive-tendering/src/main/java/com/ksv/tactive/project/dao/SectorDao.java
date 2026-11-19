package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.Sector;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SectorDao extends JpaRepository<Sector, UUID>{
	Sector findBySectorNameIgnoreCase(String sectorName);
    Optional<Sector> findBySectorNameIgnoreCaseAndActiveTrue(String sectorName);
}
