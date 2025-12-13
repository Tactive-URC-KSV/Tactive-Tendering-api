package com.ksv.tactive.master.dao;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ksv.tactive.master.entity.UOM;
public interface UOMDao extends JpaRepository<UOM, String> {
	UOM findByUomCode(String uomCode);
	UOM findByUomCodeIgnoreCase(String uomCode);
}
