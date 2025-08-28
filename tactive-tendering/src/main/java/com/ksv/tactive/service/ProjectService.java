package com.ksv.tactive.service;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.DocumentException;
import com.ksv.tactive.dto.CostCodeDto;
import com.ksv.tactive.dto.FeasibilityApprovalDocuDTO;
import com.ksv.tactive.dto.ProjectInfoDto;
import com.ksv.tactive.dto.ProjectInputDTO;
import com.ksv.tactive.dto.TemplateDto;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.project.entity.ActivityGroup;
import com.ksv.tactive.project.entity.ApprovalDocuments;
import com.ksv.tactive.project.entity.BOQ;
import com.ksv.tactive.project.entity.FeasibilityStudy;
import com.ksv.tactive.project.entity.FinancialFeasibility;
import com.ksv.tactive.project.entity.Projects;
import com.ksv.tactive.project.entity.TechnicalFeasibility;
import com.ksv.tactive.project.entity.Template;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public interface ProjectService {

	List<ProjectInfoDto> getAllProjects();
	Projects saveProject(ProjectInputDTO projectInfoDto, HttpServletRequest request) throws SQLIntegrityConstraintViolationException;
	Projects findByProjectId(String projectId);
	String deleteProject(String projectId);
	String saveProjectFiles(String projectId, List<MultipartFile> file);
	void editProject(ProjectInputDTO projectInputDTO, String projectId);
	
	List<ProjectInfoDto> getAllApprovedProjects();
	List<ProjectInfoDto> getAllBoqUploadedProject();
	
	String saveTechFeasibility(TechnicalFeasibility techFeasibility);
	String saveFinancialFeasibility(FinancialFeasibility financialFeasibility);
	String saveApprovalDoc(Map<String, MultipartFile> approvalDoc, List<FeasibilityApprovalDocuDTO> approvalDocuDTO,
			String techFeasibilityId, String projectId);
	String saveFeasibilityStudy(String techFeasibilityId, String finFeasibilityId, String projectId);
	String updateFeasibilityStudyStatus(String projectId, boolean status, String comments, HttpServletRequest request);
	FeasibilityStudy findFeasibilityStudy(String projectId);
	
	List<ApprovalDocuments> getApprovalDocData(String techId);
	//Map<ApprovalDocuments, Resource> getApprovalDocs(List<ApprovalDocuments> approvalDoc);
	
	
	List<String> getExcelSheets(MultipartFile boqFile) throws IOException;
	List<String> getPdfColumns(MultipartFile boqFile) throws IOException;
	List<String> getExcelColumns(MultipartFile boqFile, String sheetName) throws IOException;
	String saveBoqFile(MultipartFile boqFile, String projectId);
	String mapBOQ(String projectId, String sheetName, Map<String, String> columnMapping, String filePath) throws IOException;
	List<BOQ> getAllBOQ(String projectId);
	List<BOQ> getParentBOQ(List<BOQ> boqList);
	double getParentTotalValue(List<BOQ> boqList, String parentCode);
	void generateBoqExcel(String projectId, HttpServletResponse response) throws IOException, DataNotFoundException;
	void generateBoqPdf(String projectId, HttpServletResponse response) throws IOException, DocumentException, DataNotFoundException;
	void deleteBOQ(List<String> boqCode, String projectId);
	
	String saveTemplate(Template template, Map<String, String> mapping);
	List<Template> getAllTemplate();
	TemplateDto getTemplateById(String templateId);
	
	String addActivityGroup(ActivityGroup activityGroup);
	List<ActivityGroup> getAllActivityGroups();
	String saveCostCode(String projectId, List<CostCodeDto> costCodeDto);
	String deleteCostCode(String costCodeId);
	
	
}
