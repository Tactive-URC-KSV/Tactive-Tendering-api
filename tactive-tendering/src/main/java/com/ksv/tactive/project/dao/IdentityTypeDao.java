package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.IdentityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public interface IdentityTypeDao extends JpaRepository<IdentityType, UUID> {
    Optional<IdentityType> findByIdTypeIgnoreCaseAndActiveTrue(String idType);
    IdentityType findByIdType(String idType);
}
