package com.ksv.tactive.master.dao;

import com.ksv.tactive.master.entity.TaxType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaxTypeDao extends JpaRepository<TaxType, String> {
}
