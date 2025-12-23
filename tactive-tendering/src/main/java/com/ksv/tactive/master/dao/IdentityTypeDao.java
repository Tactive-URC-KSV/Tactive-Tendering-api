package com.ksv.tactive.master.dao;

import com.ksv.tactive.master.entity.IdentityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface IdentityTypeDao extends JpaRepository<IdentityType, String> {
    Optional<IdentityType> findByIdTypeIgnoreCaseAndActiveTrue(String idType);
}
