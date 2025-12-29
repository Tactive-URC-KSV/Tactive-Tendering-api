package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.CompanyDirector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyDirectorDao extends JpaRepository<CompanyDirector, String> {
}
