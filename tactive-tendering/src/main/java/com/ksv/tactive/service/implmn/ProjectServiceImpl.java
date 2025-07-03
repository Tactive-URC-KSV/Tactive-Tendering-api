package com.ksv.tactive.service.implmn;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.ksv.tactive.dao.ProjectDao;
import com.ksv.tactive.dao.RegionDao;
import com.ksv.tactive.dao.ScopeOfPackDao;
import com.ksv.tactive.dao.SectorDao;
import com.ksv.tactive.dao.UOMDao;
import com.ksv.tactive.entity.Projects;
import com.ksv.tactive.entity.Region;
import com.ksv.tactive.entity.ScopeOfPackage;
import com.ksv.tactive.exception.DuplicateProjectCodeException;
import com.ksv.tactive.exception.ProjectNotFoundException;
import com.ksv.tactive.service.ProjectService;

@Service
public class ProjectServiceImpl implements ProjectService{
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private ProjectDao projectDao; 
	
	@Autowired
	private RegionDao regionRepo;
	
	@Autowired
	private UOMDao uomDao;
	@Autowired
	private SectorDao sectorDao;
	
	@Autowired
	private ScopeOfPackDao scopeOfPackDao;
	
	@Override
	public List<Projects> getAllProjects() {
		return projectDao.findAll();
	}

	@Override
	public Projects findByProjectId(String projectId) {
		return projectDao.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(messageSource.getMessage("project.id.notfound", null, Locale.ENGLISH)));
		
	}
	
	@Override
	public Projects saveProject(Projects project, String regionId, String sectorId, List<String> scopePackagesIds,
			String uomId) throws SQLIntegrityConstraintViolationException {
		if(projectDao.findByProjectCode(project.getProjectCode()) != null) {
			throw new DuplicateProjectCodeException(messageSource.getMessage("project.code.duplicate", null , Locale.ENGLISH));
		}
		project.setId(UUID.randomUUID().toString());
		project.setEstimatedValue(project.getBuildingArea() * project.getRatePerUnit());
		project.setRegion(regionRepo.findById(regionId).get());
		project.setSector(sectorDao.findById(sectorId).get());
		project.setUom(uomDao.findById(uomId).get());
		Set<ScopeOfPackage> scopePackages = scopePackagesIds.stream()
			    .map(id -> scopeOfPackDao.findById(id).get())
			    .collect(Collectors.toSet());
		project.setScopeOfPackages(scopePackages);
		Projects savedProject = projectDao.save(project);
		if(savedProject != null) {
			return savedProject;
		}
		throw new SQLIntegrityConstraintViolationException(messageSource.getMessage("project.created.exception", null, Locale.ENGLISH));
	}

	@Override
	public String deleteProject(String projectId) {
		Projects project=findByProjectId(projectId);
		if(project!=null) {
			projectDao.delete(project);
			return messageSource.getMessage("project.delete.success", null , Locale.ENGLISH);
		}
		return null;
	}

	@Override
	public List<Region> getAllRegions() {
		return regionRepo.findAllByOrderByCountryAsc();
	}

	
	
	
	
	
	
}
