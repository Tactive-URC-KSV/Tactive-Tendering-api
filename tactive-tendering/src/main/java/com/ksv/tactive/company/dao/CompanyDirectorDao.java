package com.ksv.tactive.company.dao;

import com.ksv.tactive.company.entity.CompanyDirector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyDirectorDao extends JpaRepository<CompanyDirector, String> {
}
