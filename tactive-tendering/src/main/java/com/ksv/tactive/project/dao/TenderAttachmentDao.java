package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.TenderAttachments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenderAttachmentDao extends JpaRepository<TenderAttachments, UUID> {
    Optional<TenderAttachments> findByTenderId(UUID tenderId);
}
