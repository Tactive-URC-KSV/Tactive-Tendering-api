package com.ksv.tactive.project.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.TemplateMapping;

@Repository
public interface TemplateMappingDao extends JpaRepository<TemplateMapping, Integer>{

	List<TemplateMapping> findByTemplateId(String templateId);
	
}
