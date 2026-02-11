package com.ksv.tactive.project.dao;
import com.ksv.tactive.project.entity.Uom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UomDao extends JpaRepository<Uom, String> {
	Uom findByUomCode(String uomCode);
	Uom findByUomCodeIgnoreCase(String uomCode);
    Optional<Uom> findByUomCodeIgnoreCaseAndActiveTrue(String uomCode);
	Optional<Uom> findByUomNameIgnoreCaseAndActiveTrue(String uomName);
}
