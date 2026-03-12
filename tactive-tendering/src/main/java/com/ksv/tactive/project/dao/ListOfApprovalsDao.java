package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.ListOfApprovals;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ListOfApprovalsDao extends JpaRepository<ListOfApprovals, UUID>{
	ListOfApprovals findByDocumentName(String documentName);
    Optional<ListOfApprovals> findByDocumentNameIgnoreCaseAndActiveTrue(String documentName);
}
