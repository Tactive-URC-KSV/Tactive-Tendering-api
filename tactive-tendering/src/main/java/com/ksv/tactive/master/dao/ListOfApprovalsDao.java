package com.ksv.tactive.master.dao;
import com.ksv.tactive.master.entity.UOM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.master.entity.ListOfApprovals;

import java.util.Optional;

@Repository
public interface ListOfApprovalsDao extends JpaRepository<ListOfApprovals, String>{
	ListOfApprovals findByDocumentName(String documentName);
    Optional<UOM> findByDocumentNameIgnoreCaseAndActiveTrue(String documentName);
}
