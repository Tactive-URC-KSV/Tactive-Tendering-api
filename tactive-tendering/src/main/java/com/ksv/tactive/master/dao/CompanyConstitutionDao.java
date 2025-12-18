package com.ksv.tactive.master.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.master.entity.CompanyConstitution;
@Repository
public interface CompanyConstitutionDao extends JpaRepository<CompanyConstitution, String>{
}
