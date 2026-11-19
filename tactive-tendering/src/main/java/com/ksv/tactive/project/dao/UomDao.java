package com.ksv.tactive.project.dao;
import com.ksv.tactive.project.entity.Uom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UomDao extends JpaRepository<Uom, UUID> {
	List<Uom> findAllByUomCode(String uomCode);
    List<Uom> findByUomCodeIgnoreCaseAndActiveTrue(String uomCode);
	List<Uom> findByUomNameIgnoreCaseAndActiveTrue(String uomName);
    List<Uom> findByUomCodeIgnoreCaseAndUomNameIgnoreCase(String uomCode, String uomName);
}
