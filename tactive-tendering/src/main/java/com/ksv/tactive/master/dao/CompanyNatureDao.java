package com.ksv.tactive.master.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.master.entity.CompanyNature;
@Repository
public interface CompanyNatureDao extends JpaRepository<CompanyNature, String>{
    boolean existsByComNatureIgnoreCase(String comNature);
}
