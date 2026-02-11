package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.TaxType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaxTypeDao extends JpaRepository<TaxType, String> {
    Optional<TaxType> findByTaxTypeIgnoreCaseAndActiveTrue(String taxType);
    TaxType findByTaxType(String taxType);
}
