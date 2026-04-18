package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.TenderInvites;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenderInviteDao extends JpaRepository<TenderInvites , UUID> {
    Optional<TenderInvites> findByContractor_IdAndTender_Id(UUID contractorId, UUID tenderId);
    Optional<TenderInvites> findByTempToken(String username);
    boolean existsByTenderIdAndContractorId(UUID id, UUID id1);
}
