package com.ksv.tactive.master.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.master.entity.QuantityType;
@Repository
public interface QuantityTypeDao extends JpaRepository<QuantityType, String>{
	QuantityType findByQuantityTypeIgnoreCase(String quantityType);
}
