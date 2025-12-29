package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ksv.tactive.project.entity.UOM;

import java.util.Optional;

public interface UOMDao extends JpaRepository<UOM, String> {
	UOM findByUomCode(String uomCode);
	UOM findByUomCodeIgnoreCase(String uomCode);
    Optional<UOM> findByUomCodeIgnoreCaseAndActiveTrue(String uomCode);
	Optional<UOM> findByUomNameIgnoreCaseAndActiveTrue(String uomName);
}
