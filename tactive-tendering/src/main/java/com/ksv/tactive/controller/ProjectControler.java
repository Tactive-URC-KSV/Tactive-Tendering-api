package com.ksv.tactive.controller;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ksv.tactive.dto.FeasibilityApprovalDocuDTO;
import com.ksv.tactive.dto.ProjectInfoDto;
import com.ksv.tactive.dto.ProjectInputDTO;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.exception.DuplicateEntryException;
import com.ksv.tactive.exception.DuplicateProjectCodeException;
import com.ksv.tactive.exception.ProjectNotFoundException;
import com.ksv.tactive.project.entity.ApprovalDocuments;
import com.ksv.tactive.project.entity.FeasibilityStudy;
import com.ksv.tactive.project.entity.FinancialFeasibility;
import com.ksv.tactive.project.entity.ListOfApprovals;
import com.ksv.tactive.project.entity.ProjectStatus;
import com.ksv.tactive.project.entity.Projects;
import com.ksv.tactive.project.entity.Region;
import com.ksv.tactive.project.entity.ScopeOfPackage;
import com.ksv.tactive.project.entity.Sector;
import com.ksv.tactive.project.entity.TechnicalFeasibility;
import com.ksv.tactive.project.entity.UOM;
import com.ksv.tactive.service.ProjectService;

import jakarta.servlet.http.HttpServletRequest;


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
		List<ProjectInfoDto> projects = projectService.getAllProjects();
		if (projects.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(messageSource.getMessage("project.all.notfound", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.OK).body(projects);
	}
	
	@PostMapping("project/createProject")
	public ResponseEntity<?> createProject(@RequestBody ProjectInputDTO projectInputDTO,
			HttpServletRequest request) {
		
		try {
			Projects savedProject=projectService.saveProject(projectInputDTO,request);
			return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
		} catch (SQLIntegrityConstraintViolationException | DuplicateProjectCodeException | DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(e.getMessage());
		}
	}
	
	@PostMapping("project/saveProjectFiles/{projectId}")
	public ResponseEntity<?> saveProjectFiles(@RequestPart(required = false) List<MultipartFile> files,
			@PathVariable String projectId) {
		projectService.saveProjectFiles(projectId, files);
		return ResponseEntity.status(HttpStatus.CREATED).body("Files Uploaded");

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
	@PostMapping("/regions/addRegion")
	public ResponseEntity<String> addRegion(@RequestBody Region region) {
		try {
			return ResponseEntity.status(HttpStatus.OK).body(projectService.addRegion(region));
		} catch (DuplicateEntryException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(e.getMessage());
		}
	}
	
	@GetMapping("/regions")
	public ResponseEntity<?> getAllRegions(){
		if(projectService.getAllRegions().isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.OK).body(projectService.getAllRegions());
	}
	@PostMapping("/sector/addSector")
	public ResponseEntity<String> addSector(@RequestBody Sector sector){
		return ResponseEntity.status(HttpStatus.OK).body(projectService.addSector(sector));
	}
	
	@PostMapping("/scope/addScope")
	public ResponseEntity<String> addScope(@RequestBody ScopeOfPackage scope){
		return ResponseEntity.status(HttpStatus.OK).body(projectService.addScope(scope));
	}
	
	@PostMapping("/uom/addUom")
	public ResponseEntity<String> addUOM(@RequestBody UOM uom){
		return ResponseEntity.status(HttpStatus.OK).body(projectService.addUom(uom));
	}
	
	@GetMapping("/uoms")
    public ResponseEntity<List<UOM>> getAllUOMs() {
        List<UOM> uoms = projectService.getAllUOMs();
        return ResponseEntity.ok(uoms);
    }

    @GetMapping("/sectors")
    public ResponseEntity<List<Sector>> getAllSectors() {
        List<Sector> sectors = projectService.getAllSector();
        return ResponseEntity.ok(sectors);
    }

    @GetMapping("/scopes")
    public ResponseEntity<List<ScopeOfPackage>> getAllScopes() {
        List<ScopeOfPackage> scopes = projectService.getAllScopes();
        return ResponseEntity.ok(scopes);
    }
    
    @GetMapping("/project-status")
	public ResponseEntity<List<ProjectStatus>> getProjectStatus(){
		return ResponseEntity.ok(projectService.getAllProjectStatus());
	}
    
    @PostMapping("/project-status/add")
    public ResponseEntity<String> addProjectStatus(@RequestBody ProjectStatus projectStatus) {
		return ResponseEntity.status(HttpStatus.OK).body(projectService.addProjectStatus(projectStatus));
    }
    @PostMapping("/approvalDocuments/add")
    public ResponseEntity<String> addListOfApprovals(@RequestBody ListOfApprovals listOfApprovals) {
		return ResponseEntity.status(HttpStatus.OK).body(projectService.addListOfApprovals(listOfApprovals));
    }
    @GetMapping("/listOfApprovals")
	public ResponseEntity<List<ListOfApprovals>> getAllListOfApprovals(){
		return ResponseEntity.ok(projectService.getAllListOfApprovals());
	}
    
    
    @PostMapping("/feasibility/add/{projectId}")
    public ResponseEntity<String> addTechFeasbility(@RequestPart TechnicalFeasibility techFeasibility,
    		@RequestPart List<FeasibilityApprovalDocuDTO> approvalDocDTO,
    		@RequestPart FinancialFeasibility financialFeasibility,
    		@RequestParam Map<String , MultipartFile> approvalDoc,
    		@PathVariable String projectId){
    	System.out.println(approvalDocDTO);
    	String techFeasibilityId = projectService.saveTechFeasibility(techFeasibility);
    	String finFeasibilityId = projectService.saveFinancialFeasibility(financialFeasibility);
    	String response = projectService.saveApprovalDoc(approvalDoc, approvalDocDTO, techFeasibilityId, projectId);
    	if (response != null) {
    		String message = projectService.saveFeasibilityStudy(techFeasibilityId, finFeasibilityId, projectId);
    		return ResponseEntity.status(HttpStatus.OK).body(message);
		}
    	return ResponseEntity.status(HttpStatus.CONFLICT).body(messageSource.getMessage("feasibility.study.save.error", null, Locale.ENGLISH));
    }
    
    @PutMapping("/feasibility/approve/{projectId}")
    public ResponseEntity<String> approveFeasibilityStudy(@PathVariable String projectId, @RequestParam(required = false) String comments, HttpServletRequest request){
    	return ResponseEntity.status(HttpStatus.OK).body(projectService.updateFeasibilityStudyStatus(projectId, true, comments, request));
    }
    
    @PutMapping("/feasibility/reject/{projectId}")
    public ResponseEntity<String> rejectFeasibilityStudy(@PathVariable String projectId,
			@RequestParam(required = false) String comments, HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.OK).body(projectService.updateFeasibilityStudyStatus(projectId, false, comments, request));
	}
    
    @GetMapping("/feasibility/{projectId}")
    public ResponseEntity<?> getFeasibilityStudyByProjectId(@PathVariable String projectId) {
    	try {
    		FeasibilityStudy feaStudy = projectService.findFeasibilityStudy(projectId);
    		return ResponseEntity.status(HttpStatus.OK).body(feaStudy);
    	}
    	catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}
    
    @GetMapping("/project/approved/projects")
    public ResponseEntity<?> getApprovedProjects() {
		return null;
	}
    
    @GetMapping("/feasibility/approvalDoc/{techId}")
    public ResponseEntity<?> getFeasibilityApprovalDoc(@PathVariable String techId){
    	List<ApprovalDocuments> approvalDoc = projectService.getApprovalDocData(techId);
    	return ResponseEntity.status(HttpStatus.OK).body(approvalDoc);
//    	Map<ApprovalDocuments, Resource> approvalDocMap = projectService.getApprovalDocs(approvalDoc);
//    	return ResponseEntity.status(HttpStatus.OK).body(approvalDocMap);
    }
}