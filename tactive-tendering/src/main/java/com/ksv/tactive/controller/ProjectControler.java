package com.ksv.tactive.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.ksv.tactive.dto.*;
import com.ksv.tactive.project.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.exception.DuplicateProjectCodeException;
import com.ksv.tactive.exception.FileNotSaveException;
import com.ksv.tactive.exception.ProjectNotFoundException;
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
		try {
			List<ProjectInfoDto> projects = projectService.getAllProjects();
			return ResponseEntity.status(HttpStatus.OK).body(projects);
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
		}
	}

	@PostMapping("project/createProject")
	public ResponseEntity<?> createProject(@RequestBody ProjectInputDTO projectInputDTO, HttpServletRequest request) {
		try {
			Projects savedProject = projectService.saveProject(projectInputDTO, request);
			return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
		} catch (SQLIntegrityConstraintViolationException | DuplicateProjectCodeException | DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@PostMapping("project/saveProjectFiles/{projectId}")
	public ResponseEntity<?> saveProjectFiles(@RequestPart(required = false) List<MultipartFile> files,
											  @PathVariable String projectId) {
		projectService.saveProjectFiles(projectId, files);
		return ResponseEntity.status(HttpStatus.CREATED).body("Files Uploaded");
	}

	@GetMapping("project/viewProjectInfo/{projectId}")
	public ResponseEntity<?> findProjectById(@PathVariable String projectId) {
		try {
			return ResponseEntity.status(HttpStatus.OK).body(projectService.findByProjectId(projectId));
		} catch (ProjectNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@PutMapping("project/updateProject/{projectId}")
	public ResponseEntity<?> updateProject(@RequestBody ProjectInputDTO projectInputDTO,
										   @PathVariable String projectId) {
		try {
			projectService.editProject(projectInputDTO, projectId);
			return ResponseEntity.status(HttpStatus.OK)
					.body(messageSource.getMessage("project.update.success", null, Locale.ENGLISH));
		} catch (ProjectNotFoundException | DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@DeleteMapping("project/deleteProject/{projectId}")
	public ResponseEntity<String> deleteProject(@PathVariable String projectId) {
		try {
			return ResponseEntity.status(HttpStatus.OK).body(projectService.deleteProject(projectId));
		} catch (ProjectNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@PostMapping("/feasibility/add/{projectId}")
	public ResponseEntity<String> addTechFeasbility(@RequestPart TechnicalFeasibility techFeasibility,
													@RequestPart List<FeasibilityApprovalDocuDTO> approvalDocDTO,
													@RequestPart FinancialFeasibility financialFeasibility,
													@RequestParam Map<String, MultipartFile> approvalDoc, @PathVariable String projectId) {
		String techFeasibilityId = projectService.saveTechFeasibility(techFeasibility);
		String finFeasibilityId = projectService.saveFinancialFeasibility(financialFeasibility);
		projectService.saveApprovalDoc(approvalDoc, approvalDocDTO, techFeasibilityId, projectId);
		String message = projectService.saveFeasibilityStudy(techFeasibilityId, finFeasibilityId, projectId);
		if (message != null) {
			return ResponseEntity.status(HttpStatus.OK).body(message);
		}
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(messageSource.getMessage("feasibility.study.save.error", null, Locale.ENGLISH));
	}

	@PutMapping("/feasibility/approve/{projectId}")
	public ResponseEntity<String> approveFeasibilityStudy(@PathVariable String projectId,
														  @RequestParam(required = false) String comments, HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(projectService.updateFeasibilityStudyStatus(projectId, true, comments, request));
	}

	@PutMapping("/feasibility/reject/{projectId}")
	public ResponseEntity<String> rejectFeasibilityStudy(@PathVariable String projectId,
														 @RequestParam(required = false) String comments, HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(projectService.updateFeasibilityStudyStatus(projectId, false, comments, request));
	}

	@GetMapping("/feasibility/{projectId}")
	public ResponseEntity<?> getFeasibilityStudyByProjectId(@PathVariable String projectId) {
		try {
			FeasibilityStudy feaStudy = projectService.findFeasibilityStudy(projectId);
			return ResponseEntity.status(HttpStatus.OK).body(feaStudy);
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@PostMapping("/project/addDrawings/{projectId}")
	public ResponseEntity<?> addDrawings(@PathVariable String projectId,
										 @RequestPart(required = true) List<MultipartFile> drawings) {
		projectService.saveProjectDrawings(projectId, drawings);
		return ResponseEntity.status(HttpStatus.OK)
				.body(messageSource.getMessage("drawings.upload.success", null, Locale.ENGLISH));
	}

	@GetMapping("/project/approved/projects")
	public ResponseEntity<?> getApprovedProjects() {
		try {
			List<ProjectInfoDto> approvedProjects = projectService.getAllApprovedProjects();
			return ResponseEntity.status(HttpStatus.OK).body(approvedProjects);
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
		}

	}

	@GetMapping("/feasibility/approvalDoc/{techId}")
	public ResponseEntity<?> getFeasibilityApprovalDoc(@PathVariable String techId) {
		List<ApprovalDocuments> approvalDoc = projectService.getApprovalDocData(techId);
		return ResponseEntity.status(HttpStatus.OK).body(approvalDoc);

	}

	@GetMapping("/project/approvalDoc/{docId}")
	public ResponseEntity<?> getApprovalDocumentById(@PathVariable String docId) {
		try {
			Resource document = projectService.getApprovalDocs(docId);
			return ResponseEntity.ok(document);
		} catch (MalformedURLException | FileNotSaveException | DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@PostMapping("/project/excel/getSheets")
	public ResponseEntity<?> getBOQFileData(@RequestParam("file") MultipartFile boqFile) {
		List<String> columnNames;
		List<String> sheetNames;
		try {
			String fileName = boqFile.getOriginalFilename();
			if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
				sheetNames = projectService.getExcelSheets(boqFile);
				return ResponseEntity.status(HttpStatus.OK).body(sheetNames);
			} else if (fileName.endsWith(".pdf")) {
				columnNames = projectService.getPdfColumns(boqFile);
				return ResponseEntity.status(HttpStatus.OK).body(columnNames);
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(messageSource.getMessage("file.type.not.supported", null, Locale.ENGLISH));
			}
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
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

	@PostMapping(value = "/project/extractedBOQ", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> getExtractedBOQData(@RequestParam("file") MultipartFile boqFile,
												 @RequestParam("sheetName") String sheetName,
												 @RequestPart("columnMapping") Map<String, String> columnMapping, @RequestParam("startRow") int startRow,
												 @RequestParam("endRow") int endRow, @RequestParam(defaultValue = "0") int page,
												 @RequestParam(defaultValue = "50") int size, @RequestParam(required = false) String search) {
		try {
			PaginatedResponse<ExcelExtractBOQ> response = projectService.extractBOQDataPaginated(sheetName,
					columnMapping, boqFile, startRow, endRow, page, size, search);
			return ResponseEntity.ok(response);

		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error processing Excel file: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: " + e.getMessage());
		}
	}

	//    @PostMapping("project/mapBOQ/{sheetName}/{projectId}")
//    public ResponseEntity<?> uploadBOQ(
//    		@PathVariable String projectId,
//    		@RequestPart("file") MultipartFile boqFile,
//    		@PathVariable String sheetName,
//    		@RequestPart Map<String, String> columnMapping){
//    	String filePath = projectService.saveBoqFile(boqFile, projectId);
//    	try {
//			String response = projectService.mapBOQ(projectId, sheetName, columnMapping, filePath);
//			return ResponseEntity.status(HttpStatus.OK).body(response);
//		} catch (IOException | DataNotFoundException e) {
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//					.body("Error processing the Excel file: " + e.getMessage());
//		}
//    }
	@PostMapping(value = "project/mapBOQ/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> mapBoqData(@PathVariable String projectId, @RequestPart("file") MultipartFile boqFile,
										@RequestParam String sheetName,
										@RequestParam String columnMapping,
										@RequestParam String parentChildMapping,
										@RequestParam String lastLevelMapping,
										@RequestParam String levelMapping) {

		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, String> columnMap = mapper.readValue(columnMapping, new TypeReference<Map<String, String>>() {
			});
			Map<Long, Long> parentChildMap = mapper.readValue(parentChildMapping, new TypeReference<Map<Long, Long>>() {
			});
			Map<Long, Boolean> lastLevelMap = mapper.readValue(lastLevelMapping, new TypeReference<Map<Long, Boolean>>() {
			});
			Map<Long, Long> levelMap = mapper.readValue(levelMapping, new TypeReference<Map<Long, Long>>() {
			});

			String response = projectService.mapBOQData(
					projectId, sheetName, columnMap, parentChildMap, lastLevelMap, levelMap, boqFile
			);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} catch (IOException | DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error processing the Excel file: " + e.getMessage());
		}
	}

	@GetMapping("project/getParentBoq/{projectId}")
	public ResponseEntity<?> getParentBOQ(@PathVariable String projectId) {
		try {
			List<BOQ> boqList = projectService.getParentBOQ(projectId);
			return ResponseEntity.status(HttpStatus.OK).body(boqList);
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
		}
	}

	@GetMapping("project/getChildBoq/{projectId}/{parentBoqId}")
	public ResponseEntity<?> getChildBOQ(@PathVariable String projectId, @PathVariable String parentBoqId) {
		try {
			List<BOQ> boqList = projectService.getChildBOQ(projectId, parentBoqId);
			return ResponseEntity.status(HttpStatus.OK).body(boqList);
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
		}
	}

	@GetMapping("project/getBoqTotalValue/{projectId}")
	public ResponseEntity<?> getBoqTotalValue(@PathVariable String projectId) {
		try {
			List<BOQ> boqList = projectService.getParentBOQ(projectId);
			double totalValue = boqList.stream().mapToDouble(b -> b.getTotalAmount() != 0 ? b.getTotalAmount() : 0.0)
					.sum();
			return ResponseEntity.status(HttpStatus.OK).body(totalValue);
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@GetMapping("project/getParentTotalValue/{projectId}/{parentCode}")
	public ResponseEntity<?> getParentTotalValue(@PathVariable String projectId, @PathVariable String parentCode) {
		try {
			List<BOQ> boqList = projectService.getParentBOQ(projectId);
			double totalValue = projectService.getParentTotalValue(boqList, parentCode);
			return ResponseEntity.status(HttpStatus.OK).body(totalValue);
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@PostMapping("project/saveTemplateMapping")
	public ResponseEntity<?> saveTemplate(@RequestPart Template template, @RequestPart Map<String, String> mapping) {
		String response = projectService.saveTemplate(template, mapping);
		if (response != null) {
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(messageSource.getMessage("template.save.error", null, Locale.ENGLISH));
	}

	@GetMapping("project/getAllTemplate")
	public ResponseEntity<?> getAllTemplate() {
		List<Template> templates = projectService.getAllTemplate();
		if (templates.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body(messageSource.getMessage("template.not.found", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.OK).body(templates);
	}

	@GetMapping("project/getTemplate/{templateId}")
	public ResponseEntity<?> getTemplateById(@PathVariable String templateId) {
		try {
			TemplateDto template = projectService.getTemplateById(templateId);
			return ResponseEntity.status(HttpStatus.OK).body(template);
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@GetMapping("project/getBOQCount/{projectId}")
	public ResponseEntity<?> getBOQCount(@PathVariable String projectId) {
		try {
			Map<String, Integer> boqstats = projectService.getBOQCount(projectId);
			return ResponseEntity.status(HttpStatus.OK).body(boqstats);
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@DeleteMapping("project/deleteBOQ/{projectId}")
	public ResponseEntity<?> deleteBOQ(@RequestBody List<String> boqCode, @PathVariable String projectId) {
		try {
			projectService.deleteBOQ(boqCode, projectId);
			return ResponseEntity.status(HttpStatus.OK)
					.body(messageSource.getMessage("boq.delete.success", null, Locale.ENGLISH));
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(messageSource.getMessage("boq.delete.error", null, Locale.ENGLISH));
		}
	}

	@GetMapping("/project/BOQ/{boqId}")
	public ResponseEntity<?> getBoqDetailById(@PathVariable String boqId) {
		try {
			BOQ boq = projectService.getBoqById(boqId);
			return ResponseEntity.status(HttpStatus.OK).body(boq);
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@GetMapping("project/Boq/excel/{projectId}")
	public ResponseEntity<?> generateBOQExcel(HttpServletResponse response, @PathVariable String projectId) {
		try {
			projectService.generateBoqExcel(projectId, response);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (IOException | DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
		}
	}

	@GetMapping("project/Boq/pdf/{projectId}")
	public ResponseEntity<?> generateBOQPdf(HttpServletResponse response, @PathVariable String projectId) {
		try {
			projectService.generateBoqPdf(projectId, response);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (IOException | DocumentException | DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
		}
	}

	@GetMapping("project/getAllBoqUploadedProject")
	public ResponseEntity<?> getAllBoqProjects() {
		try {
			List<ProjectInfoDto> projects = projectService.getAllBoqUploadedProject();
			return ResponseEntity.status(HttpStatus.OK).body(projects);
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
		}
	}

	@PostMapping("/activityGroup")
	public ResponseEntity<?> addActivityGroup(@RequestBody ActivityGroupDto activityGroupDto) {
		return ResponseEntity.status(HttpStatus.OK).body(projectService.addActivityGroup(activityGroupDto));
	}

	@PutMapping("/activityGroup/edit")
	public ResponseEntity<?> editActivityGroup(@RequestBody ActivityGroupDto activityGroupDto) {
		projectService.editActivityGroup(activityGroupDto);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated", null, Locale.ENGLISH));
	}

	@GetMapping("/activityGroups")
	public ResponseEntity<?> getAllActivityGroups() {
		return ResponseEntity.status(HttpStatus.OK).body(projectService.getAllActivityGroups());
	}

	@PostMapping("costCode/saveOneToOne")
	public ResponseEntity<?> saveCostCodeActivityOneToOne(@RequestBody List<CostCodeDto> costCodeDto) {
		try {
			return ResponseEntity.status(HttpStatus.OK).body(projectService.saveCostCodeOneToOne(costCodeDto));
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@PostMapping("costCode/saveOneToMany")
	public ResponseEntity<?> saveCostCodeActivityOneToMany(@RequestBody List<CostCodeDto> costCodeDto) {
		try {
			return ResponseEntity.status(HttpStatus.OK).body(projectService.saveCostCodeOneToMany(costCodeDto));
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@PostMapping("costCode/saveManyToOne")
	public ResponseEntity<?> saveCostCodeActivityManyToOne(@RequestBody List<CostCodeDto> costCodeDto) {
		try {
			return ResponseEntity.status(HttpStatus.OK).body(projectService.saveCostCodeManyToOne(costCodeDto));
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@DeleteMapping("costCode/delete/{costCodeId}")
	public ResponseEntity<?> deleteCostCodeActivity(@PathVariable String costCodeId) {
		return ResponseEntity.status(HttpStatus.OK).body(projectService.deleteCostCode(costCodeId));
	}

	@GetMapping("project/costCodeUpdatedProjects")
	public ResponseEntity<?> getAllCostCodeUpdatedProjects() {
		List<ProjectInfoDto> projects = projectService.getAllCostCodeUpdatedProject();
		return ResponseEntity.status(HttpStatus.OK).body(projects);
	}

	@GetMapping("costCodeActivity/costCode/{costCodeId}")
	public ResponseEntity<?> getCostCodeActivityById(@PathVariable String costCodeId) {
		try {
			CostCodeActivity costCode = projectService.getCostCodeActivityById(costCodeId);
			return ResponseEntity.status(HttpStatus.OK).body(costCode);
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@GetMapping("costCodeActivity/{projectId}")
	public ResponseEntity<?> getCostCodeActivityByProject(@PathVariable String projectId) {
		List<CostCodeActivity> costCode = projectService.getCostCodeActivityByProject(projectId);
		return ResponseEntity.status(HttpStatus.OK).body(costCode);
	}

	@GetMapping("costCodeActivity/totalAmount/{costCodeTypeId}/{projectId}")
	public ResponseEntity<?> getTotalAmountByCostCodeType(@PathVariable String costCodeTypeId,
														  @PathVariable String projectId) {
		double totalAmount = projectService.getTotalAmountByCostCodeType(costCodeTypeId, projectId);
		return ResponseEntity.status(HttpStatus.OK).body(totalAmount);
	}

	@GetMapping("costCodeActivity/{costCodeTypeId}/{projectId}")
	public ResponseEntity<?> getCostCodeActivityByTypeAndProject(@PathVariable String costCodeTypeId,
																 @PathVariable String projectId) {
		List<CostCodeActivity> costCode = projectService.getCostCodeActivityByProject(projectId);
		if (!costCode.isEmpty()) {
			List<CostCodeActivity> filteredList = costCode.stream()
					.filter(c -> c.getCostCodeType().getId().equals(costCodeTypeId)).toList();
			if (!filteredList.isEmpty()) {
				return ResponseEntity.status(HttpStatus.OK).body(filteredList);
			}
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(messageSource.getMessage("cost.code.type.not.found", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(messageSource.getMessage("cost.code.not.found", null, Locale.ENGLISH));
	}

	@PostMapping("tenderEstimation/addResources")
	public ResponseEntity<?> addResourcesToTender(@RequestBody TenderResourceDto tenderResourceDto) {
		try {
			return ResponseEntity.status(HttpStatus.OK).body(projectService.addResourcesToTender(tenderResourceDto));
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@PostMapping("tenderEstimation/pasteResourceToBoq/{boqId}")
	public ResponseEntity<?> pasteResource(@PathVariable String boqId, @RequestBody List<String> tenderEstimationIds) {
		try {
			String message = projectService.pasteResourceToBOQ(boqId, tenderEstimationIds);
			return ResponseEntity.status(HttpStatus.OK).body(message);
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@GetMapping("tenderEstimation/estimatedResources/{boqId}")
	public ResponseEntity<?> getEstimatedResourcesByBoq(@PathVariable String boqId) {
		try {
			List<TenderEstResourceDto> estimatedResources = projectService.getEstimatedResourcesByBoq(boqId);
			return ResponseEntity.status(HttpStatus.OK).body(estimatedResources);
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@DeleteMapping("tenderEstimation/deleteResourceFromBoq/{tenderEstimationId}/{boqId}")
	public ResponseEntity<?> deleteTenderEstimationForBoq(@PathVariable String tenderEstimationId,
														  @PathVariable String boqId) {
		try {
			String message = projectService.deleteTenderEstimationForBoq(tenderEstimationId, boqId);
			return ResponseEntity.status(HttpStatus.OK).body(message);
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@GetMapping("tender/estimatedResource/{tenderEstimationId}")
	public ResponseEntity<?> getEstimatedResourceById(@PathVariable String tenderEstimationId) {
		try {
			TenderEstResourceDto estimatedResource = projectService.getEstimatedResourceById(tenderEstimationId);
			return ResponseEntity.status(HttpStatus.OK).body(estimatedResource);
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@GetMapping("/estimatedResource/{tenderEstimationId}")
	public TenderEstResourceDto getEstimatedResourceByIdAndBoqId(
			@PathVariable String tenderEstimationId,
			@RequestParam String boqId) {
		return projectService.getEstimatedResourceByIdAndBoqId(tenderEstimationId, boqId);
	}

	@GetMapping("project/getParentBoqAndAmount/{boqId}")
	public ResponseEntity<?> getParentBOQAndAmount(@PathVariable String boqId) {
		try {
			double parentBoqAmount = projectService.findTotalAmountForParentBoqs(boqId);
			return ResponseEntity.status(HttpStatus.OK).body(parentBoqAmount);
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@PostMapping(value = "project/tender", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> addTender(@RequestPart TenderInputDto tenderInputDto,
									   @RequestParam(value = "technicalTerms", required = false) List<MultipartFile> technicalTerms,
									   @RequestParam(value = "commercialTerms", required = false) List<MultipartFile> commercialTerms,
									   @RequestParam(value = "drawings", required = false) List<MultipartFile> drawings,
									   @RequestParam(value = "others", required = false) List<MultipartFile> others,
									   @RequestParam(value = "technicalTermsNotes", required = false) String technicalNotes,
									   @RequestParam(value = "commercialTermsNotes", required = false) String commercialNotes,
									   @RequestParam(value = "drawingsNotes", required = false) String drawingsNotes,
									   @RequestParam(value = "othersNotes", required = false) String othersNotes,
									   HttpServletRequest request){
		projectService.addTender(tenderInputDto,technicalNotes,technicalTerms,commercialNotes,commercialTerms,drawingsNotes,drawings,othersNotes,others,request);
		return ResponseEntity.status(HttpStatus.OK).body("Tender Floated Successfully");
	}
	@GetMapping("/project/tenderFloatedProject")
	public ResponseEntity<?> getTenderFloatedProject(){
		List<ProjectInfoDto> project = projectService.getTenderFloatedProject();
		return ResponseEntity.status(HttpStatus.OK).body(project);
	}
	@GetMapping("/tenders/{projectId}")
	public ResponseEntity<?> getTenderByProject(@PathVariable String projectId){
		List<Tender> tender = projectService.getAllTenderByProject(projectId);
		return ResponseEntity.status(HttpStatus.OK).body(tender);
	}
    @GetMapping("/tenderAttachments/{tenderId}")
    public ResponseEntity<?> getTenderAttachments(@PathVariable String tenderId){
        try{
            return ResponseEntity.status(HttpStatus.OK).body(projectService.getAllAttachementsByTenderId(tenderId));
        }
        catch (DataNotFoundException e){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
        }
    }
	@GetMapping("/tender/{tenderId}")
	public ResponseEntity<?> getTenderByTenderId(@PathVariable String tenderId){
		try{
			return ResponseEntity.status(HttpStatus.OK).body(projectService.getTenderByTenderId(tenderId));
		}
		catch (DataNotFoundException e){
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}
	@GetMapping("/tenderDetails/{tenderId}")
	public ResponseEntity<?> getTenderDetailsByTenderId(@PathVariable String tenderId){
		try{
			return ResponseEntity.status(HttpStatus.OK).body(projectService.getTenderDetailsByTenderId(tenderId));
		}
		catch (DataNotFoundException e){
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}
	@GetMapping("/editHistory/tender/{tenderId}")
	public ResponseEntity<?> getEditHistoryByTenderId(@PathVariable String tenderId){
		List<TenderEditHistory> editHistories = projectService.getEditHistoriesByTenderId(tenderId);
		return ResponseEntity.status(HttpStatus.OK).body(editHistories);
	}
	@GetMapping("/tender-offers/{tenderId}")
	public ResponseEntity<?> getTenderOffersByTenderId(@PathVariable String tenderId){
		List<TenderOffers> offers = projectService.getAllOfferByTenderId(tenderId);
		return ResponseEntity.status(HttpStatus.OK).body(offers);
	}
	@PutMapping("/offer/negotiate")
	public ResponseEntity<?> offerNegotiate(@RequestBody NegotiateDTO negotiateDTO){
		String message = projectService.negotiateTender(negotiateDTO);
		return ResponseEntity.status(HttpStatus.OK).body(message);
	}
	@PutMapping("/tender/award")
	public ResponseEntity<?> awardTender(@RequestParam String tenderId,
										 @RequestParam String tenderOfferId,
										 @RequestParam String offerQuotationId){
		String response = projectService.awardTender(tenderId,tenderOfferId, offerQuotationId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}