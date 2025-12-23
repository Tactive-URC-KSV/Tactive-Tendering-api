package com.ksv.tactive.master.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.master.entity.CompanyType;
@Repository
public interface CompanyTypeDao extends JpaRepository<CompanyType, String>{
    boolean existsByTypeIgnoreCase(String type);
}
