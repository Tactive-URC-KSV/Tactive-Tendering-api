package com.ksv.tactive.controller;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ksv.tactive.dto.ProjectStatusDto;
import com.ksv.tactive.entity.Projects;
import com.ksv.tactive.entity.Region;
import com.ksv.tactive.exception.DuplicateProjectCodeException;
import com.ksv.tactive.exception.ProjectNotFoundException;
import com.ksv.tactive.service.ProjectService;
import com.ksv.tactive.util.ProjectStatus;

@RestController
@RequestMapping("/tactive")
@CrossOrigin(origins = "*")
public class ProjectControler {
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private ProjectService projectService;

	@GetMapping("project/allProjects")
	public ResponseEntity<?> getAllProjects() {
		List<Projects> projects = projectService.getAllProjects();
		if (projects.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(messageSource.getMessage("project.all.notfound", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.OK).body(projects);
	}
	
	@PostMapping("project/createProject")
	public ResponseEntity<?> createProject(@RequestBody Projects project,
			@RequestParam String regionId,
			@RequestParam String sectorId,
			@RequestParam List<String> scopePackagesIds,
			@RequestParam String uomId) {
		
		try {
			Projects savedProject=projectService.saveProject(project,regionId,sectorId,scopePackagesIds,uomId);
			return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
		} catch (SQLIntegrityConstraintViolationException | DuplicateProjectCodeException  e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(e.getMessage());
		}
	}
	
	@GetMapping("/project-status")
	public ResponseEntity<List<ProjectStatusDto>> getProjectStatus(){
		return ResponseEntity.status(HttpStatus.OK)
				.body(Arrays.stream(ProjectStatus.values())
						.map(status -> new ProjectStatusDto(status.name(), status.getDisplayName()))
						.toList());
	}
	
	@GetMapping("project/viewProjectInfo/{projectId}")
	public ResponseEntity<?>  findProjectById(@PathVariable String projectId){
		try {
			return ResponseEntity.status(HttpStatus.OK)
					.body(projectService.findByProjectId(projectId));		
		}catch(ProjectNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(e.getMessage());
		}
	}
	
	@DeleteMapping("project/deleteProject/{projectId}")
	public ResponseEntity<String> deleteProject(@PathVariable String projectId){
		try {
			return ResponseEntity.status(HttpStatus.OK)
					.body(projectService.deleteProject(projectId));
		}catch(ProjectNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(e.getMessage());
		}
	}	
	
	@GetMapping("/regions")
	public ResponseEntity<List<Region>> getAllRegions(){
		return ResponseEntity.status(HttpStatus.OK).body(projectService.getAllRegions());
	}
}