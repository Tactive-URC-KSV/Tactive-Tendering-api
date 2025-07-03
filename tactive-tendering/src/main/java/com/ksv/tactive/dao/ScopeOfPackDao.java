package com.ksv.tactive.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.entity.ScopeOfPackage;

@Repository
public interface ScopeOfPackDao extends JpaRepository<ScopeOfPackage, String>{

}
