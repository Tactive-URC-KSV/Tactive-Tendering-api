package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.TenderInvites;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenderInviteDao extends JpaRepository<TenderInvites , String> {
    Optional<TenderInvites> findByContractorId(String contractorId);
    Optional<TenderInvites> findByContractor_IdAndTender_Id(String contractorId, String tenderId);
    Optional<TenderInvites> findByTempToken(String username);
    boolean existsByTenderIdAndContractorId(String id, String id1);
}
