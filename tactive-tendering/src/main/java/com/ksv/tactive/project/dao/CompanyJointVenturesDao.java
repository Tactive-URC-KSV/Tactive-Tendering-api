package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.CompanyJointVenture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CompanyJointVenturesDao extends JpaRepository<CompanyJointVenture, UUID> {

}
