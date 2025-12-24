package com.ksv.tactive.company.dao;

import com.ksv.tactive.company.entity.CompanyLocalName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyLocalNameDao extends JpaRepository<CompanyLocalName, String> {
}
