package com.ksv.tactive.service;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ksv.tactive.dto.*;
import com.ksv.tactive.project.entity.*;
import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.DocumentException;
import com.ksv.tactive.exception.DataNotFoundException;

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
	List<ProjectInfoDto> getAllCostCodeUpdatedProject();
	String saveTechFeasibility(TechnicalFeasibility techFeasibility);
	String saveFinancialFeasibility(FinancialFeasibility financialFeasibility);
	String saveApprovalDoc(Map<String, MultipartFile> approvalDoc, List<FeasibilityApprovalDocuDTO> approvalDocuDTO,
			String techFeasibilityId, String projectId);
	String saveFeasibilityStudy(String techFeasibilityId, String finFeasibilityId, String projectId);
	String updateFeasibilityStudyStatus(String projectId, boolean status, String comments, HttpServletRequest request);
	FeasibilityStudy findFeasibilityStudy(String projectId);
	void saveProjectDrawings(String projectId, List<MultipartFile> drawings);
	List<ApprovalDocuments> getApprovalDocData(String techId);
	Resource getApprovalDocs(String docId) throws MalformedURLException;
	List<String> getExcelSheets(MultipartFile boqFile) throws IOException;
	List<String> getPdfColumns(MultipartFile boqFile) throws IOException;
	List<String> getExcelColumns(MultipartFile boqFile, String sheetName) throws IOException;
	String saveBoqFile(MultipartFile boqFile, String projectId);
//	String mapBOQ(String projectId, String sheetName, Map<String, String> columnMapping, String filePath) throws IOException;
	String mapBOQData(String projectId, String sheetName, Map<String, String> columnMapping,
			Map<Long, Long> parentChildMapping, Map<Long, Boolean> lastLevelMapping, Map<Long, Long> levelMapping,
			MultipartFile boqFile) throws IOException;
	List<BOQ> getParentBOQ(String projectId);
	List<BOQ> getChildBOQ(String projectId, String parentBoqId);
	List<BOQ> getParentBOQ(List<BOQ> boqList);
	Map<String, Integer> getBOQCount(String projectId);
	double findTotalAmountForParentBoqs(String boqId);
	void generateBoqExcel(String projectId, HttpServletResponse response) throws IOException, DataNotFoundException;
	void generateBoqPdf(String projectId, HttpServletResponse response) throws IOException, DocumentException, DataNotFoundException;
	void deleteBOQ(List<String> boqCode, String projectId) throws DataIntegrityViolationException;
	BOQ getBoqById(String boqId);
	String saveTemplate(Template template, Map<String, String> mapping);
	List<Template> getAllTemplate();
	TemplateDto getTemplateById(String templateId);
	String addActivityGroup(ActivityGroupDto activityGroupDto);
	void editActivityGroup(ActivityGroupDto activityGroupDto);
	List<ActivityGroup> getAllActivityGroups();
	String saveCostCodeOneToOne(List<CostCodeDto> costCodeDto);
	String saveCostCodeOneToMany(List<CostCodeDto> costCodeDto);
	String saveCostCodeManyToOne(List<CostCodeDto> costCodeDto);
	String deleteCostCode(String costCodeId);
	List<CostCodeActivity> getCostCodeActivityByProject(String projectId);
	double getTotalAmountByCostCodeType(String costCodeTypeId, String projectId);
	CostCodeActivity getCostCodeActivityById(String costCodeId);
	ActivityGroup getActivityGroupById(String activityGroupId);
	String addResourcesToTender(TenderResourceDto tenderResourceDto);
	List<TenderEstResourceDto> getEstimatedResourcesByBoq(String boqId);
//	List<TenderEstimation> getEstimatedResourcesByActivityGroup(String activityGroupId);
//	String pasteResourceToActivity(String activityGroupId, List<String> tenderEstimationIds);
//	String updateTenderEstimationResource(TenderResourceDto tenderResourceDto);
	String deleteTenderEstimationForBoq(String tenderEstimationId, String boqId);
	TenderEstResourceDto getEstimatedResourceById(String tenderEstimationId);
	TenderEstResourceDto getEstimatedResourceByIdAndBoqId(String tenderEstimationId, String boqId);
	PaginatedResponse<ExcelExtractBOQ> extractBOQDataPaginated(String sheetName,
			Map<String, String> columnMapping, MultipartFile boqFile, int startRow, int endRow, int page, int size, String search) throws IOException;
    void addTender(TenderInputDto tenderInputDto, String technicalNotes, List<MultipartFile> technicalTerms, String commercialNotes,
				   List<MultipartFile> commercialTerms, String drawingsNotes, List<MultipartFile> drawings, String othersNotes, List<MultipartFile> others, HttpServletRequest request);
	List<ProjectInfoDto> getTenderFloatedProject();
	List<Tender> getAllTenderByProject(String projectId);
	TenderAttachments getAllAttachementsByTenderId(String tenderId);
	TenderRespDto getTenderByTenderId(String tenderId);
	TenderOffers saveOffer(OfferDTO offerDTO);
    Tender getTenderDetailsByTenderId(String tenderId);
	List<TenderEditHistory> getEditHistoriesByTenderId(String tenderId);
	List<TenderOffers> getAllOfferByTenderId(String tenderId);
	String negotiateTender(NegotiateDTO negotiateDTO);
	TenderRespDto findOfferByOfferVersion(String tenderId, String contractorId, int offerVersion);
	int findOfferNegotiation(String negotiationId);
	String awardTender(String tenderId, String tenderOfferId, String offerQuotationId);
	List<BOQ> searchBoq(String projectId, String searchQuery);
	Map<String, Double> getParentBoqTotalMap(String projectId);
    List<ProjectInfoDto> getAllProjectsByCompanyId(String companyId);
	String updateBOQHierarchy(String projectId, Map<String, String> parentChildUpdateMap);
	Page<BOQ> getParentBOQs(String projectId, int page, int size);
    String addGlobalValue(String projectId, GlobalValue globalValue);
	List<GlobalValue> viewAllGlobalValueByProjectId(String projectId);
	String editGlobalValue(String id, GlobalValue globalValue);
	List<InternalBoq> getInternalBoqByProject(UUID projectId);
	InternalBoq getInternalBoqById(String internalBoqId);
}
