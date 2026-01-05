package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.ContractorFormInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractorFormInviteDao extends JpaRepository<ContractorFormInvite, String> {
    Optional<ContractorFormInvite> findByUsername(String username);
}
