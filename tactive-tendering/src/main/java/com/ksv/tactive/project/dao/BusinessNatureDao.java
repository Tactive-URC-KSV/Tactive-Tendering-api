package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.BusinessNature;

import java.util.UUID;

@Repository
public interface BusinessNatureDao extends JpaRepository<BusinessNature, UUID>{
    boolean existsByBusinessNatureIgnoreCase(String businessNature);
}
