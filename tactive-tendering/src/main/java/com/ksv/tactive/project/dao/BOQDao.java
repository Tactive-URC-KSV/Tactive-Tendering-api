package com.ksv.tactive.project.dao;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.BOQ;
@Repository
public interface BOQDao extends JpaRepository<BOQ, UUID>{
	List<BOQ> findByProjectId(String projectId);
	
	List<BOQ> findByProjectIdAndSourceFileAndExcelSnoOrderByCreatedAtDesc(String projectId, String sourceFile, Long excelSno);
	@org.springframework.data.jpa.repository.Query("SELECT COALESCE(MAX(b.pageNo), 0) FROM BOQ b WHERE b.projectId = :projectId AND b.sourceFile = :sourceFile")
	Long findMaxPageNoByProjectIdAndSourceFile(
			@org.springframework.data.repository.query.Param("projectId") String projectId,
			@org.springframework.data.repository.query.Param("sourceFile") String sourceFile);
	@org.springframework.data.jpa.repository.Query("SELECT COALESCE(MAX(b.excelSno), 0) FROM BOQ b WHERE b.projectId = :projectId AND b.sourceFile = :sourceFile")
	Long findMaxExcelSnoByProjectIdAndSourceFile(
			@org.springframework.data.repository.query.Param("projectId") String projectId,
			@org.springframework.data.repository.query.Param("sourceFile") String sourceFile);
	List<BOQ> findByProjectIdOrderByExcelSnoAscCreatedAtAsc(String projectId);
	List<BOQ> findByProjectIdAndParentBOQId(String projectId, UUID parentBoqId);
	List<BOQ> findByProjectIdAndParentBOQIdOrderByExcelSnoAscCreatedAtAsc(String projectId, UUID parentBoqId);
	List<BOQ> findByProjectIdAndLevelOrderByExcelSnoAscCreatedAtAsc(String projectId, int level);
//    List<BOQ> findByProjectIdAndBoqNameContainingIgnoreCase(UUID projectId, String searchQuery);
    Optional<BOQ> findByProjectIdAndBoqCodeIgnoreCaseAndBoqNameIgnoreCase(String id, String boqCode, String boqName);
    Page<BOQ> findByProjectIdAndParentBOQIsNull(String projectId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT b.division FROM BOQ b WHERE b.projectId = :projectId AND b.division IS NOT NULL AND b.division != ''")
    List<String> findDistinctDivisionsByProjectId(@org.springframework.data.repository.query.Param("projectId") String projectId);

    Page<BOQ> findByProjectIdAndDivisionOrderByExcelSnoAscCreatedAtAsc(
            String projectId, String division, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT b FROM BOQ b WHERE b.projectId = :projectId AND (b.division IS NULL OR b.division = '') ORDER BY b.excelSno ASC, b.createdAt ASC")
    Page<BOQ> findByProjectIdAndNoDivision(String projectId, Pageable pageable);
}
