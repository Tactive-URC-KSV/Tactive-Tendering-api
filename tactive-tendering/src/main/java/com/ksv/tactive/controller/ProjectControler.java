package com.ksv.tactive.controller;

import java.io.IOException;
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

import com.itextpdf.text.DocumentException;
import com.ksv.tactive.dto.FeasibilityApprovalDocuDTO;
import com.ksv.tactive.dto.ProjectInfoDto;
import com.ksv.tactive.dto.ProjectInputDTO;
import com.ksv.tactive.dto.TemplateDto;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.exception.DuplicateEntryException;
import com.ksv.tactive.exception.DuplicateProjectCodeException;
import com.ksv.tactive.exception.ProjectNotFoundException;
import com.ksv.tactive.project.entity.ApprovalDocuments;
import com.ksv.tactive.project.entity.BOQ;
import com.ksv.tactive.project.entity.FeasibilityStudy;
import com.ksv.tactive.project.entity.FinancialFeasibility;
import com.ksv.tactive.project.entity.ListOfApprovals;
import com.ksv.tactive.project.entity.ProjectStatus;
import com.ksv.tactive.project.entity.Projects;
import com.ksv.tactive.project.entity.Region;
import com.ksv.tactive.project.entity.ScopeOfPackage;
import com.ksv.tactive.project.entity.Sector;
import com.ksv.tactive.project.entity.TechnicalFeasibility;
import com.ksv.tactive.project.entity.Template;
import com.ksv.tactive.project.entity.UOM;
import com.ksv.tactive.service.ProjectService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


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
	
	@PutMapping("project/updateProject/{projectId}")
	public ResponseEntity<?> updateProject(@RequestBody ProjectInputDTO projectInputDTO,
			@PathVariable String projectId){
		try {
			projectService.editProject(projectInputDTO, projectId);
			return ResponseEntity.status(HttpStatus.OK)
					.body(messageSource.getMessage("project.update.success", null, Locale.ENGLISH));
		}
		catch (ProjectNotFoundException | DataNotFoundException e) {
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
    	List<ProjectInfoDto> approvedProjects = projectService.getAllApprovedProjects();
		return ResponseEntity.status(HttpStatus.OK).body(approvedProjects);
	}
    
    @GetMapping("/feasibility/approvalDoc/{techId}")
    public ResponseEntity<?> getFeasibilityApprovalDoc(@PathVariable String techId){
    	List<ApprovalDocuments> approvalDoc = projectService.getApprovalDocData(techId);
    	return ResponseEntity.status(HttpStatus.OK).body(approvalDoc);
//    	Map<ApprovalDocuments, Resource> approvalDocMap = projectService.getApprovalDocs(approvalDoc);
//    	return ResponseEntity.status(HttpStatus.OK).body(approvalDocMap);
    }
    
    @PostMapping("/project/excel/getSheets")
    public ResponseEntity<?> getBOQFileData(@RequestParam("file") MultipartFile boqFile) {
    	List<String> columnNames;
    	List<String> sheetNames;
		try {
			String fileName = boqFile.getOriginalFilename();
			if(fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
				sheetNames = projectService.getExcelSheets(boqFile);
				return ResponseEntity.status(HttpStatus.OK).body(sheetNames);
			}
			else if(fileName.endsWith(".pdf")){
				columnNames = projectService.getPdfColumns(boqFile);
				return ResponseEntity.status(HttpStatus.OK).body(columnNames);
			}
			else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(messageSource.getMessage("file.type.not.supported", null, Locale.ENGLISH));
			}
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(e.getMessage());
		}
    	
    }
    
    @PostMapping("/project/excel/getColumns/{sheetName}")
    public ResponseEntity<?> getColumns(@RequestParam("file") MultipartFile boqFile, @PathVariable String sheetName) {
    	List<String> columnNames;
    	try {
			columnNames = projectService.getExcelColumns(boqFile, sheetName);
			return ResponseEntity.status(HttpStatus.OK).body(columnNames);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error reading the Excel file: " + e.getMessage());
		}
    }
    
    @PostMapping("project/mapBOQ/{sheetName}/{projectId}")
    public ResponseEntity<?> uploadBOQ(
    		@PathVariable String projectId,
    		@RequestPart("file") MultipartFile boqFile,
    		@PathVariable String sheetName,
    		@RequestPart Map<String, String> columnMapping){
    	String filePath = projectService.saveBoqFile(boqFile, projectId);
    	try {
			String response = projectService.mapBOQ(projectId, sheetName, columnMapping, filePath);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error processing the Excel file: " + e.getMessage());
		}
    }
    
    @GetMapping("project/getParentBoq/{projectId}")
    public ResponseEntity<?> getParentBOQ(@PathVariable String projectId){
    	List<BOQ> boqList =projectService.getAllBOQ(projectId);
    	List<BOQ> parentBoqList = projectService.getParentBOQ(boqList);
    	if(boqList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body(messageSource.getMessage("boq.not.found", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.OK).body(parentBoqList);
    }
    
    @GetMapping("project/getBoqTotalValue/{projectId}")
    public ResponseEntity<?> getBoqTotalValue(@PathVariable String projectId){
    	List<BOQ> boqList =projectService.getAllBOQ(projectId);
    	if(boqList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body(messageSource.getMessage("boq.not.found", null, Locale.ENGLISH));
		}
    	double totalValue = boqList.stream()
    			.mapToDouble(b -> b.getTotalAmount() != 0 ? b.getTotalAmount() : 0.0)
    			.sum();
		return ResponseEntity.status(HttpStatus.OK).body(totalValue);
    }

    @GetMapping("project/getParentTotalValue/{projectId}/{parentCode}")
    public ResponseEntity<?> getParentTotalValue(@PathVariable String projectId, @PathVariable String parentCode){
    	List<BOQ> boqList =projectService.getAllBOQ(projectId);
    	if(boqList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body(messageSource.getMessage("boq.not.found", null, Locale.ENGLISH));
		}
    	double totalValue = projectService.getParentTotalValue(boqList, parentCode);
		return ResponseEntity.status(HttpStatus.OK).body(totalValue);
    }
    
    @PostMapping("project/saveTemplateMapping")
    public ResponseEntity<?> saveTemplate(@RequestPart Template template,
    		@RequestPart Map<String, String> mapping){
    	String response = projectService.saveTemplate(template,mapping);
    	if(response != null) {
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}
		return ResponseEntity.status(HttpStatus.CONFLICT).body(messageSource.getMessage("template.save.error", null, Locale.ENGLISH));

    }
    
    @GetMapping("project/getAllTemplate")
    public ResponseEntity<?> getAllTemplate(){
    	List<Template> templates = projectService.getAllTemplate();
		if(templates.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body(messageSource.getMessage("template.not.found", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.OK).body(templates);
    }
    
    @GetMapping("project/getTemplate/{templateId}")
    public ResponseEntity<?> getTemplateById(@PathVariable String templateId){
    	try {
    		TemplateDto template = projectService.getTemplateById(templateId);
        	return ResponseEntity.status(HttpStatus.OK).body(template);
    	}catch (DataNotFoundException e) {
    		return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    	}
    }
    
    @GetMapping("project/getAllBOQ/{projectId}")
    public ResponseEntity<?> getAllBOQ(@PathVariable String projectId){
    	try {
    		
    		List<BOQ> boqList =projectService.getAllBOQ(projectId);
    		return ResponseEntity.status(HttpStatus.OK).body(boqList);
    	}
    	catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
    }
    
    @DeleteMapping("project/deleteBOQ/{projectId}")
    public ResponseEntity<?> deleteBOQ(@RequestBody List<String> boqCode, @PathVariable String projectId) {
    	projectService.deleteBOQ(boqCode, projectId);
    	return null;
    }
    
    @GetMapping("project/Boq/excel/{projectId}")
    public ResponseEntity<?> generateBOQExcel(HttpServletResponse response, @PathVariable String projectId) {
    	try {
    		projectService.generateBoqExcel(projectId,response);
    		return ResponseEntity.status(HttpStatus.OK).body(null);
    	}
    	catch(IOException | DataNotFoundException e) {
    		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
    	}
    }
    @GetMapping("project/Boq/pdf/{projectId}")
    public ResponseEntity<?> generateBOQPdf(HttpServletResponse response, @PathVariable String projectId) {
    	try {
			projectService.generateBoqPdf(projectId,response);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (IOException | DocumentException | DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
		}
    }
    
    @GetMapping("project/getAllBoqUploadedProject")
    public ResponseEntity<?> getAllBoqProjects(){
    	List<ProjectInfoDto> projects = projectService.getAllBoqUploadedProject();
    	return ResponseEntity.status(HttpStatus.OK).body(projects);
    }
		
}