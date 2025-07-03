package com.ksv.tactive.service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ksv.tactive.entity.Projects;
import com.ksv.tactive.entity.Region;

@Service
public interface ProjectService {

	List<Projects> getAllProjects();
	Projects saveProject(Projects project, String regionId, String sectorId, List<String> scopePackagesIds, String uomId) throws SQLIntegrityConstraintViolationException;
	Projects findByProjectId(String projectId);
	String deleteProject(String projectId);
	List<Region> getAllRegions();
}
