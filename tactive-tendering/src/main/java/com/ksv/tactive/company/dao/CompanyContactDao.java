package com.ksv.tactive.company.dao;

import com.ksv.tactive.company.entity.CompanyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyContactDao extends JpaRepository<CompanyContact, String> {
}
