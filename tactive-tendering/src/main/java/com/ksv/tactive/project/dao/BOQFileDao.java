package com.ksv.tactive.project.dao;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ksv.tactive.project.entity.BOQFile;
public interface BOQFileDao extends JpaRepository<BOQFile, UUID>{
	List<BOQFile> findByProject(String projectId);
}
