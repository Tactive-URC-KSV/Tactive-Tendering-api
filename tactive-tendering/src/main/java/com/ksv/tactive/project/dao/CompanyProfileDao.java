package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyProfileDao extends JpaRepository<CompanyProfile, String> {
}
