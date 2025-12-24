package com.ksv.tactive.company.dao;

import com.ksv.tactive.company.entity.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyProfileDao extends JpaRepository<CompanyProfile, String> {
}
