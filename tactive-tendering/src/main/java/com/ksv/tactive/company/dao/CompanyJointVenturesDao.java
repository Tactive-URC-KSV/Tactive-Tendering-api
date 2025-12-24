package com.ksv.tactive.company.dao;

import com.ksv.tactive.company.entity.CompanyJointVenture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyJointVenturesDao extends JpaRepository<CompanyJointVenture, String> {

}
