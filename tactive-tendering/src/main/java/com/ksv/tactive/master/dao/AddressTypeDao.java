package com.ksv.tactive.master.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.master.entity.AddressType;

@Repository
public interface AddressTypeDao extends JpaRepository<AddressType, String>{

}
