package com.ksv.tactive.service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ksv.tactive.dto.FeasibilityApprovalDocuDTO;
import com.ksv.tactive.dto.ProjectInfoDto;
import com.ksv.tactive.dto.ProjectInputDTO;
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

import jakarta.servlet.http.HttpServletRequest;

@Service
public interface ProjectService {

	List<ProjectInfoDto> getAllProjects();
	Projects saveProject(ProjectInputDTO projectInfoDto, HttpServletRequest request) throws SQLIntegrityConstraintViolationException;
	Projects findByProjectId(String projectId);
	String deleteProject(String projectId);
	String saveProjectFiles(String projectId, List<MultipartFile> file);
	String addRegion(Region region);
	List<Region> getAllRegions();
	
	String addSector(Sector sector);
	List<Sector> getAllSector();
	
	String addScope(ScopeOfPackage scope);
	List<ScopeOfPackage> getAllScopes();
	
	String addUom(UOM uom);
	List<UOM> getAllUOMs();
	
	List<ProjectStatus> getAllProjectStatus();
	String addProjectStatus(ProjectStatus projectStatus);
	String addListOfApprovals(ListOfApprovals listOfApprovals);
	
	
	List<ListOfApprovals> getAllListOfApprovals();
	
	String saveTechFeasibility(TechnicalFeasibility techFeasibility);
	String saveFinancialFeasibility(FinancialFeasibility financialFeasibility);
	String saveApprovalDoc(Map<String, MultipartFile> approvalDoc, List<FeasibilityApprovalDocuDTO> approvalDocuDTO,
			String techFeasibilityId, String projectId);
	String saveFeasibilityStudy(String techFeasibilityId, String finFeasibilityId, String projectId);
	String updateFeasibilityStudyStatus(String projectId, boolean status, String comments, HttpServletRequest request);
	FeasibilityStudy findFeasibilityStudy(String projectId);
	
	List<ApprovalDocuments> getApprovalDocData(String techId);
	//Map<ApprovalDocuments, Resource> getApprovalDocs(List<ApprovalDocuments> approvalDoc);

	
}
