package com.ksv.tactive.project.dao;
import com.ksv.tactive.project.entity.Uom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UomDao extends JpaRepository<Uom, UUID> {
	Uom findByUomCode(String uomCode);
    Optional<Uom> findByUomCodeIgnoreCaseAndActiveTrue(String uomCode);
	Optional<Uom> findByUomNameIgnoreCaseAndActiveTrue(String uomName);
    Uom findByUomCodeIgnoreCaseAndUomNameIgnoreCase(String uomCode, String uomName);
}
