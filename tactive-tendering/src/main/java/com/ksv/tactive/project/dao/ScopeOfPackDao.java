package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.ScopeOfPackage;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScopeOfPackDao extends JpaRepository<ScopeOfPackage, UUID>{
	ScopeOfPackage findByScope(String scope);
	ScopeOfPackage findByScopeIgnoreCase(String scope);

    Optional<ScopeOfPackage> findByScopeIgnoreCaseAndActiveTrue(String scope);
}
