package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.TenderAttachments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenderAttachmentDao extends JpaRepository<TenderAttachments, String> {
    Optional<TenderAttachments> findByTenderId(String tenderId);
}
