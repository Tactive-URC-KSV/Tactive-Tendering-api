package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.CompanyLevel;
@Repository
public interface CompanyLevelDao extends JpaRepository<CompanyLevel, String>{
    boolean existsByLevelIgnoreCase(String level);
}
