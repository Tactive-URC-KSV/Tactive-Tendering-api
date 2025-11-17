package com.ksv.tactive.controller;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.ksv.tactive.dto.CostCodeDto;
import com.ksv.tactive.dto.FeasibilityApprovalDocuDTO;
import com.ksv.tactive.dto.ProjectInfoDto;
import com.ksv.tactive.dto.ProjectInputDTO;
import com.ksv.tactive.dto.TemplateDto;
import com.ksv.tactive.dto.TenderResourceDto;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.exception.DuplicateProjectCodeException;
import com.ksv.tactive.exception.FileNotSaveException;
import com.ksv.tactive.exception.ProjectNotFoundException;
import com.ksv.tactive.project.entity.ActivityGroup;
import com.ksv.tactive.project.entity.ApprovalDocuments;
import com.ksv.tactive.project.entity.BOQ;
import com.ksv.tactive.project.entity.CostCodeActivity;
import com.ksv.tactive.project.entity.FeasibilityStudy;
import com.ksv.tactive.project.entity.FinancialFeasibility;
import com.ksv.tactive.project.entity.Projects;
import com.ksv.tactive.project.entity.TechnicalFeasibility;
import com.ksv.tactive.project.entity.Template;
import com.ksv.tactive.service.ProjectService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@RestController
@RequestMapping("/tactive")
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
    @PostMapping("/project/addDrawings/{projectId}")
    public ResponseEntity<?> addDrawings(@PathVariable String projectId,
    		@RequestPart(required = true) List<MultipartFile> drawings){
    	projectService.saveProjectDrawings(projectId, drawings);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("drawings.upload.success", null, Locale.ENGLISH));
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
    	
    }
    @GetMapping("/project/approvalDoc/{docId}")
    public ResponseEntity<?> getApprovalDocumentById(@PathVariable String docId){
    	try {
    	    Resource document = projectService.getApprovalDocs(docId);
    	    return ResponseEntity.ok(document);
    	} 
    	catch (MalformedURLException | FileNotSaveException | DataNotFoundException e) {
    	    return ResponseEntity.status(HttpStatus.CONFLICT)
    	                         .body(e.getMessage());
    	}
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
		} catch (IOException | DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error processing the Excel file: " + e.getMessage());
		}
    }
    @GetMapping("project/getParentBoq/{projectId}")
    public ResponseEntity<?> getParentBOQ(@PathVariable String projectId){
    	try {
    		List<BOQ> boqList =projectService.getParentBOQ(projectId);
    		return ResponseEntity.status(HttpStatus.OK).body(boqList);
    	}
    	catch (DataNotFoundException e) {
    		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
    	}
    }
    @GetMapping("project/getChildBoq/{projectId}/{parentBoqId}")
    public ResponseEntity<?> getChildBOQ(@PathVariable String projectId, @PathVariable String parentBoqId){
    	try {
    		List<BOQ> boqList =projectService.getChildBOQ(projectId, parentBoqId);
    		return ResponseEntity.status(HttpStatus.OK).body(boqList);
    	}
    	catch (DataNotFoundException e) {	
    		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
    	}
    }
    
    @GetMapping("project/getBoqTotalValue/{projectId}")
    public ResponseEntity<?> getBoqTotalValue(@PathVariable String projectId){
    	try {
    		List<BOQ> boqList =projectService.getParentBOQ(projectId);
    		double totalValue = boqList.stream()
    				.mapToDouble(b -> b.getTotalAmount() != 0 ? b.getTotalAmount() : 0.0)
    				.sum();
    		return ResponseEntity.status(HttpStatus.OK).body(totalValue);
    	}
    	catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
    }
    @GetMapping("project/getParentTotalValue/{projectId}/{parentCode}")
    public ResponseEntity<?> getParentTotalValue(@PathVariable String projectId, @PathVariable String parentCode){
    	try {
			List<BOQ> boqList =projectService.getParentBOQ(projectId);
			double totalValue = projectService.getParentTotalValue(boqList, parentCode);
			return ResponseEntity.status(HttpStatus.OK).body(totalValue);
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
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
    @GetMapping("project/getBOQCount/{projectId}")
    public ResponseEntity<?> getBOQCount(@PathVariable String projectId){
    	try {
    		Map<String, Integer> boqstats =projectService.getBOQCount(projectId);
    		return ResponseEntity.status(HttpStatus.OK).body(boqstats);
    	}
    	catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
    }
    @DeleteMapping("project/deleteBOQ/{projectId}")
    public ResponseEntity<?> deleteBOQ(@RequestBody List<String> boqCode, @PathVariable String projectId) {
    	try {
    		projectService.deleteBOQ(boqCode, projectId);
    		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("boq.delete.success", null, Locale.ENGLISH));
    	}
    	catch (DataNotFoundException e) {
    		return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    	}
    	catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(messageSource.getMessage("boq.delete.error", null, Locale.ENGLISH));
    	}
    }
    @GetMapping("/project/BOQ/{boqId}")
    public ResponseEntity<?> getBoqDetailById(@PathVariable String boqId){
    	try {
    		BOQ boq = projectService.getBoqById(boqId);
    		return ResponseEntity.status(HttpStatus.OK).body(boq);
    	}
    	catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
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
    	try {
    		List<ProjectInfoDto> projects = projectService.getAllBoqUploadedProject();
    		return ResponseEntity.status(HttpStatus.OK).body(projects);
    	}
    	catch (DataNotFoundException e) {
    		return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    	}
    }
    @PostMapping("/activityGroup/add")
    public ResponseEntity<?> addActivityGroup(@RequestBody ActivityGroup activityGroup) {
		return ResponseEntity.status(HttpStatus.OK).body(projectService.addActivityGroup(activityGroup));
	}
    @GetMapping("/activityGroups")
    public ResponseEntity<?> getAllActivityGroups() {
    	return ResponseEntity.status(HttpStatus.OK).body(projectService.getAllActivityGroups());
	}
    @PostMapping("costCode/saveOneToOne")
    public ResponseEntity<?> saveCostCodeActivityOneToOne(@RequestBody List<CostCodeDto> costCodeDto ){
    	try {
    		return ResponseEntity.status(HttpStatus.OK).body(projectService.saveCostCodeOneToOne(costCodeDto));
    	}
    	catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
    }
    @PostMapping("costCode/saveOneToMany")
    public ResponseEntity<?> saveCostCodeActivityOneToMany(@RequestBody List<CostCodeDto> costCodeDto ){
		try {
			return ResponseEntity.status(HttpStatus.OK).body(projectService.saveCostCodeOneToMany(costCodeDto));
		}
		catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
    }
    @PostMapping("costCode/saveManyToOne")
    public ResponseEntity<?> saveCostCodeActivityManyToOne(@RequestBody List<CostCodeDto> costCodeDto ){
		try {
			return ResponseEntity.status(HttpStatus.OK).body(projectService.saveCostCodeManyToOne(costCodeDto));
		}
		catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
    }
    @DeleteMapping("costCode/delete/{costCodeId}")
    public ResponseEntity<?> deleteCostCodeActivity(@PathVariable String costCodeId ){
		return ResponseEntity.status(HttpStatus.OK).body(projectService.deleteCostCode(costCodeId));
	}
    @GetMapping("project/costCodeUpdatedProjects")
    public ResponseEntity<?> getAllCostCodeUpdatedProjects(){
		List<ProjectInfoDto> projects = projectService.getAllCostCodeUpdatedProject();
		return ResponseEntity.status(HttpStatus.OK).body(projects);
	}
    @GetMapping("costCodeActivity/costCode/{costCodeId}")
    public ResponseEntity<?> getCostCodeActivityById(@PathVariable String costCodeId){
		try {
			CostCodeActivity costCode = projectService.getCostCodeActivityById(costCodeId);
			return ResponseEntity.status(HttpStatus.OK).body(costCode);
		}catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}
    @GetMapping("costCodeActivity/{projectId}")
    public ResponseEntity<?> getCostCodeActivityByProject(@PathVariable String projectId){
    	List<CostCodeActivity> costCode = projectService.getCostCodeActivityByProject(projectId);
    	if(!costCode.isEmpty()) {
    		return ResponseEntity.status(HttpStatus.OK).body(costCode);
    	}
    	return ResponseEntity.status(HttpStatus.CONFLICT).body(messageSource.getMessage("cost.code.not.found", null, Locale.ENGLISH));
    }
    @GetMapping("costCodeActivity/totalAmount/{costCodeTypeId}/{projectId}")
    public ResponseEntity<?> getTotalAmountByCostCodeType(@PathVariable String costCodeTypeId, @PathVariable String projectId){
		double totalAmount = projectService.getTotalAmountByCostCodeType(costCodeTypeId, projectId);
		return ResponseEntity.status(HttpStatus.OK).body(totalAmount);
	}
    @GetMapping("costCodeActivity/{costCodeTypeId}/{projectId}")
    public ResponseEntity<?> getCostCodeActivityByTypeAndProject(@PathVariable String costCodeTypeId, @PathVariable String projectId){
		List<CostCodeActivity> costCode = projectService.getCostCodeActivityByProject(projectId);
		if(!costCode.isEmpty()) {
			List<CostCodeActivity> filteredList = costCode.stream()
					.filter(c -> c.getCostCodeType().getId().equals(costCodeTypeId))
					.toList();
			if(!filteredList.isEmpty()) {
				return ResponseEntity.status(HttpStatus.OK).body(filteredList);
			}
			return ResponseEntity.status(HttpStatus.CONFLICT).body(messageSource.getMessage("cost.code.type.not.found", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.CONFLICT).body(messageSource.getMessage("cost.code.not.found", null, Locale.ENGLISH));
	}
    @PostMapping("tenderEstimation/addResources")
    public ResponseEntity<?> addResourcesToTender(@RequestBody TenderResourceDto tenderResourceDto){
    	try {
    		return ResponseEntity.status(HttpStatus.OK).body(projectService.addResourcesToTender(tenderResourceDto));
    	}catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    	}
    }
    @PostMapping("tenderEstimation/pasteResourceToCostCode/{costCodeId}")
    public ResponseEntity<?> pasteResource(@PathVariable String costCodeId, @RequestBody List<String> tenderEstimationIds){
    	try {
    		String message = projectService.pasteResourceToCostCode(costCodeId, tenderEstimationIds);
    		return ResponseEntity.status(HttpStatus.OK).body(message);
    	}
    	catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
    }
   
}