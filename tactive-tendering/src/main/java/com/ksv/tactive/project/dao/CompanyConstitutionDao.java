package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.CompanyConstitution;

import java.util.UUID;

@Repository
public interface CompanyConstitutionDao extends JpaRepository<CompanyConstitution, UUID>{
    boolean existsByComConstitutionIgnoreCase(String comConstitution);
}
