package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.ContractorFormInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractorFormInviteDao extends JpaRepository<ContractorFormInvite, UUID> {
    Optional<ContractorFormInvite> findByUsername(String username);
    ContractorFormInvite findByEmail(String email);
}
