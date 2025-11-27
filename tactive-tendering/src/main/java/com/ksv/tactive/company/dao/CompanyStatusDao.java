package com.ksv.tactive.company.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.company.entity.CompanyStatus;
@Repository
public interface CompanyStatusDao extends JpaRepository<CompanyStatus, String>{
}
