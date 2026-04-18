package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.ContractorFormInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.Contractor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractorDao extends JpaRepository<Contractor, UUID>{
    Contractor findByInviteId(String inviteId);
}
