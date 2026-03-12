package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ksv.tactive.project.entity.ComAddressDetails;

import java.util.UUID;

public interface ComAddressDetailsDao extends JpaRepository<ComAddressDetails, UUID>{
}
