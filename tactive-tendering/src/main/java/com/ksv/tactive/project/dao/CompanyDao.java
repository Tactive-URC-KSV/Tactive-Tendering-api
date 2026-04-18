package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ksv.tactive.project.entity.Company;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CompanyDao extends JpaRepository<Company, UUID>{
}
