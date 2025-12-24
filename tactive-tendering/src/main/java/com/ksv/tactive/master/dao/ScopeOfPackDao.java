package com.ksv.tactive.master.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.master.entity.ScopeOfPackage;

import java.util.Optional;

@Repository
public interface ScopeOfPackDao extends JpaRepository<ScopeOfPackage, String>{
	ScopeOfPackage findByScope(String scope);
	ScopeOfPackage findByScopeIgnoreCase(String scope);

    Optional<ScopeOfPackage> findByScopeIgnoreCaseAndActiveTrue(String scope);
}
