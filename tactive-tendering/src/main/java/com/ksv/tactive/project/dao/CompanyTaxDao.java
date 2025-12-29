package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.CompanyTax;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyTaxDao extends JpaRepository<CompanyTax, String> {
}
