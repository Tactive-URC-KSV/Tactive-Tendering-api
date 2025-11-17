package com.ksv.tactive.project.dao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.BOQ;
@Repository
public interface BOQDao extends JpaRepository<BOQ, String>{
	BOQ findByBoqCodeAndProjectId(String parentCode, String projectId);
	List<BOQ> findByProjectId(String projectId);
	List<BOQ> findByProjectIdAndParentBOQId(String projectId, String parentBoqId);
}
