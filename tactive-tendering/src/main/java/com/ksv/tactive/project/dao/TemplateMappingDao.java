package com.ksv.tactive.project.dao;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.TemplateMapping;
@Repository
public interface TemplateMappingDao extends JpaRepository<TemplateMapping, UUID>{
	List<TemplateMapping> findByTemplateId(UUID templateId);
}
