package com.ksv.tactive.master.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.master.entity.CompanyLevel;
@Repository
public interface CompanyLevelDao extends JpaRepository<CompanyLevel, String>{
    boolean existsByLevelIgnoreCase(String level);
}
