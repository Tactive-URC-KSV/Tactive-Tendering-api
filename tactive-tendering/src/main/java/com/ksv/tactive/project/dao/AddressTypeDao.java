package com.ksv.tactive.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.AddressType;

@Repository
public interface AddressTypeDao extends JpaRepository<AddressType, String>{
    boolean existsByAddressTypeIgnoreCaseAndActiveTrueAndIdNot(String addressType, String id);
}
