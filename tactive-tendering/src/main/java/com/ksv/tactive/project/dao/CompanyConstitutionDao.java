package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.CompanyConstitution;
@Repository
public interface CompanyConstitutionDao extends JpaRepository<CompanyConstitution, String>{
    boolean existsByComConstitutionIgnoreCase(String comConstitution);
}
