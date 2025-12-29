package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.BusinessNature;
@Repository
public interface BusinessNatureDao extends JpaRepository<BusinessNature, String>{
    boolean existsByBusinessNatureIgnoreCase(String businessNature);
}
