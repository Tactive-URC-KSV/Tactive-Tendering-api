package com.ksv.tactive.company.dao;

import com.ksv.tactive.company.entity.CompanyTax;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyTaxDao extends JpaRepository<CompanyTax, String> {
}
