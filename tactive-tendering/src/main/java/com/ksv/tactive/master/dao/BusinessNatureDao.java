package com.ksv.tactive.master.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.master.entity.BusinessNature;
@Repository
public interface BusinessNatureDao extends JpaRepository<BusinessNature, String>{
    boolean existsByBusinessNatureIgnoreCase(String businessNature);
}
