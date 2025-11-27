package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.ListOfApprovals;
@Repository
public interface ListOfApprovalsDao extends JpaRepository<ListOfApprovals, String>{
	ListOfApprovals findByDocumentName(String documentName);
}
