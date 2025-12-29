package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.CompanyStatus;
@Repository
public interface CompanyStatusDao extends JpaRepository<CompanyStatus, String>{
    boolean existsByComStatusIgnoreCase(String comStatus);
}
