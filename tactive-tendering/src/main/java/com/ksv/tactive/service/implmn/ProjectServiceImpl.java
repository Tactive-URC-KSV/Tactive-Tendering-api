package com.ksv.tactive.service.implmn;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.DocumentException;
import com.ksv.tactive.dto.CostCodeDto;
import com.ksv.tactive.dto.ExcelExtractBOQ;
import com.ksv.tactive.dto.FeasibilityApprovalDocuDTO;
import com.ksv.tactive.dto.PaginatedResponse;
import com.ksv.tactive.dto.ProjectInfoDto;
import com.ksv.tactive.dto.ProjectInputDTO;
import com.ksv.tactive.dto.TemplateDto;
import com.ksv.tactive.dto.TenderEstResourceDto;
import com.ksv.tactive.dto.TenderResourceDto;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.exception.DuplicateProjectCodeException;
import com.ksv.tactive.exception.FileNotSaveException;
import com.ksv.tactive.exception.ProjectNotFoundException;
import com.ksv.tactive.fileservice.ExcelFileService;
import com.ksv.tactive.fileservice.PdfFileService;
import com.ksv.tactive.project.dao.ActivityGroupDao;
import com.ksv.tactive.project.dao.ApprovalsDao;
import com.ksv.tactive.project.dao.BOQDao;
import com.ksv.tactive.project.dao.BOQFileDao;
import com.ksv.tactive.project.dao.CostCodeDao;
import com.ksv.tactive.project.dao.CostCodeTypeDao;
import com.ksv.tactive.project.dao.FeasibilityStudyDao;
import com.ksv.tactive.project.dao.FinancialFeasibilityDao;
import com.ksv.tactive.project.dao.ProjectCurrencyDao;
import com.ksv.tactive.project.dao.ProjectDao;
import com.ksv.tactive.project.dao.ProjectDocumentDetailsDao;
import com.ksv.tactive.project.dao.ProjectStatusDao;
import com.ksv.tactive.project.dao.QuantityTypeDao;
import com.ksv.tactive.project.dao.RegionDao;
import com.ksv.tactive.project.dao.ResourceDao;
import com.ksv.tactive.project.dao.ResourceNatureDao;
import com.ksv.tactive.project.dao.ResourceTypeDao;
import com.ksv.tactive.project.dao.ScopeOfPackDao;
import com.ksv.tactive.project.dao.SectorDao;
import com.ksv.tactive.project.dao.TechnicalFeasibilityDao;
import com.ksv.tactive.project.dao.TemplateDao;
import com.ksv.tactive.project.dao.TemplateMappingDao;
import com.ksv.tactive.project.dao.TenderEstCostDao;
import com.ksv.tactive.project.dao.TenderEstimationDao;
import com.ksv.tactive.project.dao.UOMDao;
import com.ksv.tactive.project.entity.ActivityGroup;
import com.ksv.tactive.project.entity.ApprovalDocuments;
import com.ksv.tactive.project.entity.BOQ;
import com.ksv.tactive.project.entity.BOQFile;
import com.ksv.tactive.project.entity.CostCodeActivity;
import com.ksv.tactive.project.entity.CostCodeType;
import com.ksv.tactive.project.entity.FeasibilityStudy;
import com.ksv.tactive.project.entity.FinancialFeasibility;
import com.ksv.tactive.project.entity.ProjectDocumentDetails;
import com.ksv.tactive.project.entity.ProjectStatus;
import com.ksv.tactive.project.entity.Projects;
import com.ksv.tactive.project.entity.ScopeOfPackage;
import com.ksv.tactive.project.entity.TechnicalFeasibility;
import com.ksv.tactive.project.entity.Template;
import com.ksv.tactive.project.entity.TemplateMapping;
import com.ksv.tactive.project.entity.TenderEstimation;
import com.ksv.tactive.project.entity.TenderEstimationCostDetails;
import com.ksv.tactive.project.entity.UOM;
import com.ksv.tactive.service.ProjectService;
import com.ksv.tactive.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Service
public class ProjectServiceImpl implements ProjectService {
	@Value("${file.upload.path}")
	private String uploadDir;
//	private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);
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
	@Autowired
	private UserService userService;
	@Autowired
	private ProjectStatusDao statusDao;
	@Autowired
	private ProjectDocumentDetailsDao documentDetailsDao;
	@Autowired
	private TechnicalFeasibilityDao techFeaDao;
	@Autowired
	private ApprovalsDao approvalsDao;
	@Autowired
	private FinancialFeasibilityDao finFeasibilityDao;
	@Autowired
	private FeasibilityStudyDao feaStudyDao;
	@Autowired
	private BOQFileDao boqFileDao;
	@Autowired
	private TemplateDao templateDao;
	@Autowired
	private TemplateMappingDao templateMappingDao;
	@Autowired
	private BOQDao boqDao;
	@Autowired
	private ExcelFileService excelService;
	@Autowired
	private PdfFileService pdfService;
	@Autowired
	private ActivityGroupDao activityGroupsDao;
	@Autowired
	private CostCodeTypeDao costCodeTypeDao;
	@Autowired
	private CostCodeDao costCodeActivityDao;
	@Autowired
	private ResourceTypeDao resourceTypeDao;
	@Autowired
	private ResourceDao resourceDao;
	@Autowired
	private ResourceNatureDao resourceNatureDao;
	@Autowired
	private QuantityTypeDao quantityTypeDao;
	@Autowired
	private ProjectCurrencyDao projectCurrencyDao;
	@Autowired
	private TenderEstimationDao tenderEstDao;
	@Autowired
	private TenderEstCostDao costDetailsDao;
	@Override
	public List<ProjectInfoDto> getAllProjects() {
		List<ProjectInfoDto> projects = projectDao.findAllProjectInfo();
		List<ProjectInfoDto> filteredProjects = new ArrayList<>();
		if (projects.isEmpty()) {
			throw new DataNotFoundException(messageSource.getMessage("project.not.found", null, Locale.ENGLISH));
		} else {
			for (ProjectInfoDto project : projects) {
				if (project != null && !project.isArchieve()) {
					filteredProjects.add(project);
				}
			}
		}
		return filteredProjects;
	}
	@Override
	public Projects findByProjectId(String projectId) {
		return projectDao.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(
				messageSource.getMessage("project.not.found", null, Locale.ENGLISH)));
	}
	@Override
	public Projects saveProject(ProjectInputDTO projectInputDTO, HttpServletRequest request)
			throws SQLIntegrityConstraintViolationException {
		Projects project = projectInputDTO.getProject();
		if (projectDao.findByProjectCode(project.getProjectCode()) != null) {
			throw new DuplicateProjectCodeException(
					messageSource.getMessage("project.code.duplicate", null, Locale.ENGLISH));
		}
		project.setId(UUID.randomUUID().toString());
		project.setProjectCode(generateProjectCode());
		if (project.getProjectName() == null || project.getShortName() == null) {
			throw new DataNotFoundException(messageSource.getMessage("project.name.not.found", null, Locale.ENGLISH));
		}
		project.setEstimatedValue(project.getBuildingArea() * project.getRatePerUnit());
		if (!projectInputDTO.getRegionId().isEmpty()) {
			project.setRegion(regionRepo.findById(projectInputDTO.getRegionId()).orElseThrow(
					() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH))));
		}
		if (!projectInputDTO.getSectorId().isEmpty()) {
			project.setSector(sectorDao.findById(projectInputDTO.getSectorId()).orElseThrow(
					() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH))));
		}
		if (!projectInputDTO.getUomId().isEmpty()) {
			project.setUom(uomDao.findById(projectInputDTO.getUomId()).orElseThrow(
					() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH))));
		}
		if (!projectInputDTO.getScopeOfPackageIds().isEmpty()) {
			Set<ScopeOfPackage> scopePackages = projectInputDTO.getScopeOfPackageIds().stream()
					.filter(id -> id != null && !id.trim().isEmpty())
					.map(id -> scopeOfPackDao.findById(id)
							.orElseThrow(() -> new DataNotFoundException(
									messageSource.getMessage("data.not.found", null, Locale.ENGLISH))))
					.collect(Collectors.toSet());
			project.setScopeOfPackages(scopePackages);
		}
		project.setUpdateAt(Date.valueOf(LocalDate.now()));
		project.setCreatedAt(Date.valueOf(LocalDate.now()));
		project.setCreatedBy(userService.findLoggedInUser().getId());
		project.setCompanyId(userService.findLoggedInUser().getCompanyId());
		project.setProjectStatus(statusDao.findByProcessUrl(request.getRequestURI()));
		Projects savedProject = projectDao.save(project);
		if (savedProject != null) {
			return savedProject;
		}
		throw new SQLIntegrityConstraintViolationException(
				messageSource.getMessage("project.created.exception", null, Locale.ENGLISH));
	}
	private String generateProjectCode() {
		final String chars = "0123456789";
		Random random = new Random();
		StringBuilder code = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			int index = random.nextInt(chars.length());
			code.append(chars.charAt(index));
		}
		return "PRJ - " + code.toString();
	}
	@Override
	public String saveProjectFiles(String projectId, List<MultipartFile> files) {
		Projects project = findByProjectId(projectId);
		Path directory = Paths
				.get(uploadDir,
						project.getProjectName().trim().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9-_\\.]", ""))
				.toAbsolutePath().normalize();
		try {
			Files.createDirectories(directory);
			for (MultipartFile file : files) {
				Path targetLocation = directory.resolve(StringUtils.cleanPath(file.getOriginalFilename()));
				Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
				ProjectDocumentDetails projectDocuments = new ProjectDocumentDetails();
				projectDocuments.setId(UUID.randomUUID().toString());
				projectDocuments.setDocumentPath(targetLocation.toString());
				projectDocuments.setDocumentName(StringUtils.cleanPath(file.getOriginalFilename()));
				projectDocuments.setDocumentType(file.getContentType());
				projectDocuments.setProject(project);
				documentDetailsDao.save(projectDocuments);
			}
			return "Files Uploaded Successfully";
		} catch (IOException e) {
			return e.getMessage();
		}
	}
	@Override
	public void editProject(ProjectInputDTO projectInputDTO, String projectId) {
		Projects existingProject = findByProjectId(projectId);
		if (existingProject == null) {
			throw new DataNotFoundException(messageSource.getMessage("project.not.found", null, Locale.ENGLISH));
		}
		Projects project = projectInputDTO.getProject();
		project.setId(existingProject.getId());
		project.setCreatedAt(existingProject.getCreatedAt());
		project.setCreatedBy(existingProject.getCreatedBy());
		project.setCompanyId(existingProject.getCompanyId());
		project.setProjectStatus(existingProject.getProjectStatus());
		project.setArchieve(existingProject.isArchieve());
		project.setProjectCode(existingProject.getProjectCode());
		project.setEstimatedValue(project.getBuildingArea() * project.getRatePerUnit());
		if (!projectInputDTO.getRegionId().isEmpty()) {
			project.setRegion(regionRepo.findById(projectInputDTO.getRegionId()).orElseThrow(
					() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH))));
		}
		if (!projectInputDTO.getSectorId().isEmpty()) {
			project.setSector(sectorDao.findById(projectInputDTO.getSectorId()).orElseThrow(
					() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH))));
		}
		if (!projectInputDTO.getUomId().isEmpty()) {
			project.setUom(uomDao.findById(projectInputDTO.getUomId()).orElseThrow(
					() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH))));
		}
		if (!projectInputDTO.getScopeOfPackageIds().isEmpty()) {
			Set<ScopeOfPackage> scopePackages = projectInputDTO.getScopeOfPackageIds().stream()
					.filter(id -> id != null && !id.trim().isEmpty())
					.map(id -> scopeOfPackDao.findById(id)
							.orElseThrow(() -> new DataNotFoundException(
									messageSource.getMessage("data.not.found", null, Locale.ENGLISH))))
					.collect(Collectors.toSet());
			project.setScopeOfPackages(scopePackages);
		}
		project.setUpdateAt(Date.valueOf(LocalDate.now()));
		projectDao.save(project);
	}
	@Override
	public String deleteProject(String projectId) {
		Projects project = findByProjectId(projectId);
		project.setArchieve(true);
		if (project != null) {
			projectDao.save(project);
		}
		return messageSource.getMessage("project.delete.success", null, Locale.ENGLISH);
	}
	@Override
	public String saveTechFeasibility(TechnicalFeasibility techFeasibility) {
		techFeasibility.setId(UUID.randomUUID().toString());
		TechnicalFeasibility saved = techFeaDao.save(techFeasibility);
		return saved.getId();
	}
	@Override
	public String saveFinancialFeasibility(FinancialFeasibility financialFeasibility) {
		financialFeasibility.setId(UUID.randomUUID().toString());
		FinancialFeasibility saved = finFeasibilityDao.save(financialFeasibility);
		return saved.getId();
	}
	@Override
	public String saveApprovalDoc(Map<String, MultipartFile> approvalDoc,
			List<FeasibilityApprovalDocuDTO> approvalDocuDTO, String techFeasibilityId, String projectId) {
		if (!approvalDoc.isEmpty()) {
			Map<String, MultipartFile> filteredApprovalDoc = new HashMap<>();
			for (Map.Entry<String, MultipartFile> entry : approvalDoc.entrySet()) {
				String key = entry.getKey();
				if (key.startsWith("approvalDoc[") && key.endsWith("]")) {
					String cleanKey = key.substring(key.indexOf('[') + 1, key.indexOf(']'));
					filteredApprovalDoc.put(cleanKey, entry.getValue());
				}
			}
			Set<String> docName = filteredApprovalDoc.keySet();
			String lastSavedId = null;
			for (String doc : docName) {
				String filePath = saveApprovalDocFile(filteredApprovalDoc.get(doc), projectId);
				if (filePath == null) {
					throw new FileNotSaveException(
							messageSource.getMessage("file.upload.failure", null, Locale.ENGLISH));
				}
				ApprovalDocuments approvalDocuments = new ApprovalDocuments();
				approvalDocuments.setId(UUID.randomUUID().toString());
				for (FeasibilityApprovalDocuDTO dto : approvalDocuDTO) {
					if (dto.getDocumentName().equalsIgnoreCase(doc)) {
						approvalDocuments.setDocumentName(dto.getDocumentName());
						approvalDocuments.setComments(dto.getComment());
						approvalDocuments.setApproved(dto.isApproved());
						break;
					}
				}
				approvalDocuments.setFilePath(filePath);
				approvalDocuments.setTechnicalFeasibility(
						techFeaDao.findById(techFeasibilityId).orElseThrow(() -> new DataNotFoundException(
								messageSource.getMessage("data.not.found", null, Locale.ENGLISH))));
				lastSavedId = approvalsDao.save(approvalDocuments).getId();
			}
			return lastSavedId;
		}
		return null;
	}
	private String saveApprovalDocFile(MultipartFile file, String projectId) {
		String projectName = findByProjectId(projectId).getProjectName();
		Path directory = Paths
				.get(uploadDir, projectName.trim().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9-_\\.]", ""))
				.toAbsolutePath().normalize();
		Path targetDir = directory.resolve("approvals");
		try {
			Files.createDirectories(targetDir);
			Path targetLocation = targetDir.resolve(StringUtils.cleanPath(file.getOriginalFilename()));
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			return targetLocation.toString();
		} catch (IOException e) {
			return e.getMessage();
		}
	}
	@Override
	public String saveFeasibilityStudy(String techFeasibilityId, String finFeasibilityId, String projectId) {
		FeasibilityStudy feaStudy = new FeasibilityStudy();
		Projects project = findByProjectId(projectId);
		TechnicalFeasibility techFeasibility = techFeaDao.findById(techFeasibilityId).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		FinancialFeasibility finFeasibility = finFeasibilityDao.findById(finFeasibilityId).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		if (project != null && techFeasibility != null && finFeasibility != null) {
			feaStudy.setId(UUID.randomUUID().toString());
			feaStudy.setProject(project);
			feaStudy.setTechnFeasibility(techFeasibility);
			feaStudy.setFinancialFeasibility(finFeasibility);
			feaStudy.setFeasibilityApproved(false);
			feaStudyDao.save(feaStudy);
		}
		return messageSource.getMessage("feasibility.study.save.success", null, Locale.ENGLISH);
	}
	@Override
	public String updateFeasibilityStudyStatus(String projectId, boolean status, String comments,
			HttpServletRequest request) {
		FeasibilityStudy feasibilityStudy = feaStudyDao.findByProjectId(projectId).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		feasibilityStudy.setFeasibilityApproved(status);
		feasibilityStudy.setComments(comments);
		feasibilityStudy.setReviewedBy(userService.findLoggedInUser().getId());
		Projects project = feasibilityStudy.getProject();
		String requestUrl = request.getRequestURI();
		ProjectStatus projectStatus = statusDao.findByProcessUrl(requestUrl.substring(0, requestUrl.lastIndexOf("/")));
		if (projectStatus != null) {
			project = updateProjectStatus(project, projectStatus);
			feasibilityStudy.setProject(project);
		}
		feaStudyDao.save(feasibilityStudy);
		return messageSource.getMessage("feasibility.study.status.update.success", null, Locale.ENGLISH);
	}
	private Projects updateProjectStatus(Projects project, ProjectStatus status) {
		project.setProjectStatus(status);
		return projectDao.save(project);
	}
	@Override
	public FeasibilityStudy findFeasibilityStudy(String projectId) {
		return feaStudyDao.findByProjectId(projectId).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	}
	@Override
	public List<ApprovalDocuments> getApprovalDocData(String techId) {
		List<ApprovalDocuments> approvalDoc = approvalsDao.findAllByTechnicalFeasibilityId(techId);
		return approvalDoc;
	}
	@Override
	public void saveProjectDrawings(String projectId, List<MultipartFile> drawings) {
		Projects project = findByProjectId(projectId);
		Path directory = Paths
				.get(uploadDir,
						project.getProjectName().trim().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9-_\\.]", ""))
				.toAbsolutePath().normalize();
		Path targetDir = directory.resolve("drawings");
		try {
			Files.createDirectories(targetDir);
			for (MultipartFile file : drawings) {
				Path targetLocation = targetDir.resolve(StringUtils.cleanPath(file.getOriginalFilename()));
				Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
				ProjectDocumentDetails projectDocuments = new ProjectDocumentDetails();
				projectDocuments.setId(UUID.randomUUID().toString());
				projectDocuments.setDocumentPath(targetLocation.toString());
				projectDocuments.setDocumentName(StringUtils.cleanPath(file.getOriginalFilename()));
				projectDocuments.setDocumentType(file.getContentType());
				projectDocuments.setProject(project);
				documentDetailsDao.save(projectDocuments);
			}
		} catch (IOException e) {
			throw new FileNotSaveException(messageSource.getMessage("file.upload.failure", null, Locale.ENGLISH));
		}
	}
	@Override
	public Resource getApprovalDocs(String docId) throws MalformedURLException {
		ApprovalDocuments doc = approvalsDao.findById(docId).orElseThrow(() -> new DataNotFoundException(
				messageSource.getMessage("approval.document.not.found", null, Locale.ENGLISH)));
		Path filePath = Paths.get(doc.getFilePath());
		Resource resource = new UrlResource(filePath.toUri());
		if (resource.exists() || resource.isReadable()) {
			return resource;
		} else {
			throw new FileNotSaveException(messageSource.getMessage("file.not.found", null, Locale.ENGLISH));
		}
			
	}
	@Override
	public List<ProjectInfoDto> getAllApprovedProjects() {
		List<String> projectId = feaStudyDao.findAll().stream().filter(id -> id.isFeasibilityApproved())
				.map(project -> project.getProject().getId()).collect(Collectors.toList());
		List<ProjectInfoDto> approvedProjects = projectId.stream().map(id -> projectDao.findAllProjectInfoById(id))
				.filter(project -> !project.isArchieve()).collect(Collectors.toList());
		return approvedProjects.stream().filter(project -> project != null).collect(Collectors.toList());
	}
	@Override
	public List<String> getExcelSheets(MultipartFile boqFile) throws IOException {
		return excelService.getSheetNames(boqFile);
	}
	@Override
	public List<String> getPdfColumns(MultipartFile boqFile) throws IOException {
		return pdfService.getColumnsHeaders(boqFile);
	}
	@Override
	public List<String> getExcelColumns(MultipartFile boqFile, String sheetName) throws IOException {
		String fileName = boqFile.getOriginalFilename();
		if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
			return excelService.getColumns(boqFile, sheetName);
		}
		return null;
	}
	@Override
	public PaginatedResponse<ExcelExtractBOQ> extractBOQDataPaginated(
	        String sheetName,
	        Map<String, String> columnMapping,
	        MultipartFile boqFile,
	        int startRow,
	        int endRow,
	        int page,
	        int size,
	        String search
	) throws IOException {
		
	    List<ExcelExtractBOQ> allRows = null;
	    String fileName = boqFile.getOriginalFilename();
		if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
			allRows = excelService.getExtractedData(boqFile, sheetName, columnMapping, startRow, endRow);
		}
		else {
			allRows = pdfService.getExtractedData(boqFile, columnMapping, startRow, endRow);
		}
	    if (search != null && !search.isBlank()) {
	        String lower = search.toLowerCase();
	        allRows = allRows.stream()
	                .filter(item ->
	                        (item.getBoqCode() != null && item.getBoqCode().toLowerCase().contains(lower)) ||
	                        (item.getBoqName() != null && item.getBoqName().toLowerCase().contains(lower))
	                )
	                .toList();
	    }
	    int totalElements = allRows.size();
	    int totalPages = (int) Math.ceil((double) totalElements / size);
	    int fromIndex = page * size;
	    int toIndex = Math.min(fromIndex + size, totalElements);
	    List<ExcelExtractBOQ> content =
	            (fromIndex >= totalElements) ? List.of() : allRows.subList(fromIndex, toIndex);
	    return new PaginatedResponse<ExcelExtractBOQ>(
	            content,        
	            page,           
	            size,          
	            totalElements,  
	            totalPages      
	    );
	}
	@Override
	public String saveBoqFile(MultipartFile boqFile, String projectId) {
		String projectName = findByProjectId(projectId).getProjectName();
		Path directory = Paths
				.get(uploadDir, projectName.trim().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9-_\\.]", ""))
				.toAbsolutePath().normalize().resolve("BOQ");
		try {
			if (boqFile == null || boqFile.isEmpty()) {
				throw new FileNotSaveException("Uploaded file is empty");
			}
			String fileName = StringUtils.cleanPath(boqFile.getOriginalFilename());
			if (fileName.isBlank() || fileName.contains("..")) {
				throw new FileNotSaveException("Invalid file name: " + fileName);
			}
			Files.createDirectories(directory);
			Path targetLocation = directory.resolve(fileName);
			Files.copy(boqFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			String filePath = targetLocation.toString();
			List<BOQFile> boqFiles = boqFileDao.findByProjectId(projectId);
			if (boqFiles.stream().noneMatch(b -> b.getFilename().equals(fileName))) {
				BOQFile boqFileEntity = new BOQFile();
				boqFileEntity.setId(UUID.randomUUID().toString());
				boqFileEntity.setFilename(fileName);
				boqFileEntity.setFileUrl(filePath);
				boqFileEntity.setProject(findByProjectId(projectId));
				boqFileDao.save(boqFileEntity);
			}
			return filePath;
		} catch (IOException e) {
			throw new FileNotSaveException(messageSource.getMessage("file.upload.failure", null, Locale.ENGLISH), e);
		}
	}
//	@Override
//	public String mapBOQ(String projectId, String sheetName, Map<String, String> columnMapping, String filePath)
//			throws IOException, DataNotFoundException {
//		Projects project = findByProjectId(projectId);
//		Resource resource = new UrlResource(Paths.get(filePath).toUri());
//		List<BOQ> boqList = null;
//		if (resource.exists() || resource.isReadable()) {
//			String fileName = resource.getFilename();
//			InputStream inputStream = resource.getInputStream();
//			if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
//				boqList = excelService.mapBOQData(sheetName, columnMapping, inputStream);
//			} else if (fileName.endsWith(".pdf")) {
//				boqList = pdfService.mapBOQData(columnMapping, inputStream);
//			} else {
//				throw new FileNotSaveException(
//						messageSource.getMessage("file.type.not.supported", null, Locale.ENGLISH));
//			}
//			if (!boqList.isEmpty()) {
//				BOQ parentBoq = null;
//				BOQ subBoq = null;
//				for (BOQ boq : boqList) {
//					boq.setId(UUID.randomUUID().toString());
//					boq.setProject(project);
//					if ((!isValidText(boq.getBoqCode()) && !isValidText(boq.getBoqName()))
//				            && boq.getQuantity() == 0 && boq.getUom() == null) {
//				        continue;
//				    }
//					if((!isNotEmpty(boq.getBoqCode()) || !isNotEmpty(boq.getBoqName()))
//							&& boq.getQuantity() == 0 && boq.getUom() == null) {
//						boq.setParentBOQ(null);
//						boq.setLevel(1);
//						parentBoq=boqDao.save(boq);
//						subBoq = null;
//					}
//					else if(boq.getQuantity() == 0 && boq.getUom() == null && (boq.getBoqCode() != null && boq.getBoqName() != null)){
//						boq.setParentBOQ(parentBoq);
//						boq.setLevel(2);
//						boq.setLastLevel(false);
//						subBoq = boqDao.save(boq);
//					}
//					else if(boq.getQuantity() != 0 || boq.getUom() != null){
//						if(subBoq != null) {
//							boq.setParentBOQ(subBoq);
//						} else {
//							boq.setParentBOQ(parentBoq);
//						}
//						boq.setLevel(3);
//						boqDao.save(boq);
//					}
//				}
//			}
//			return "BOQ data mapped successfully";
//		}
//		return null;
//	}
	@Override
	public String mapBOQData(String projectId,
	                         String sheetName,
	                         Map<String, String> columnMapping,
	                         Map<Long, Long> parentChildMapping,
	                         Map<Long, Boolean> lastLevelMapping,
	                         Map<Long, Long> levelMapping,
	                         MultipartFile boqFile) throws IOException {

	    Projects project = findByProjectId(projectId);
	    saveBoqFile(boqFile, projectId);
	    int startRow = 0;
	    int endRow = 1000;
	    List<ExcelExtractBOQ> extractedRows;
	    String fileName = boqFile.getOriginalFilename();
	    if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
	        extractedRows = excelService.getExtractedData(boqFile, sheetName, columnMapping, startRow, endRow);
	    } else {
	        extractedRows = pdfService.getExtractedData(boqFile, columnMapping, startRow, endRow);
	    }
	    if (extractedRows == null || extractedRows.isEmpty()) {
	        throw new DataNotFoundException("No BOQ data extracted from file!");
	    }
	    Map<Long, BOQ> boqEntityMap = new HashMap<>();
	    extractedRows.forEach(row -> {
	        BOQ boq = new BOQ();
	        boq.setId(UUID.randomUUID().toString());
	        boq.setBoqCode(row.getBoqCode());
	        boq.setBoqName(row.getBoqName());
	        boq.setQuantity(row.getQuantity());
	        boq.setProject(project);
	        if (row.getUom() != null) {
	            UOM uom = findUOM(row.getUom());
	            boq.setUom(uom);
	        }
	        if (!isValidText(row.getBoqCode()) && !isNotEmpty(row.getBoqCode())) {
	            return;
	        }
	        Long level = levelMapping.getOrDefault(row.getSno(), 0L);
	        boq.setLevel(level.intValue());
	        if (!row.isLastLevel()) {
	            boq.setLastLevel(lastLevelMapping.getOrDefault(row.getSno(), false));
	        } else {
	            boq.setLastLevel(row.isLastLevel());
	        }

	        boqEntityMap.put(row.getSno(), boq);
	    });
	    parentChildMapping.forEach((childSno, parentSno) -> {
	        BOQ child = boqEntityMap.get(childSno);
	        BOQ parent = boqEntityMap.get(parentSno);

	        if (child != null && parent != null) {
	            child.setParentBOQ(parent);
	            parent.getChildren().add(child);
	        }
	    });
	    for (ExcelExtractBOQ row : extractedRows) {
	        Long childSno = row.getSno();
	        Long parentSno = row.getParentSno();
	        if (parentSno == null || parentSno == 0) continue;
	        if (parentChildMapping.containsKey(childSno)) continue;
	        BOQ child = boqEntityMap.get(childSno);
	        BOQ parent = boqEntityMap.get(parentSno);
	        if (child != null && parent != null) {
	            child.setParentBOQ(parent);
	            parent.getChildren().add(child);
	        }
	    }
	    List<BOQ> boqList = new ArrayList<>(boqEntityMap.values());
	    boqDao.saveAll(boqList);
	    return "BOQ Mapping Successfully Stored for Project " + projectId;
	}

	protected UOM findUOM(String value) {
		String sanitizedValue = value.trim().replaceAll("[., /]", "");
		List<UOM> uoms = uomDao.findAll();
		for (UOM uom : uoms) {
			if (uom.getUomCode().equalsIgnoreCase(sanitizedValue) || uom.getUomName().equalsIgnoreCase(value)) {
				return uom;
			}
		}
		return null;
	}

	private boolean isValidText(String value) {
	    if (value == null) return false;
	    String trimmed = value.trim();
	    if (trimmed.isEmpty()) return false;
	    return !trimmed.equalsIgnoreCase("-") &&
	           !trimmed.equalsIgnoreCase("--") &&
	           !trimmed.equalsIgnoreCase("—") &&
	           !trimmed.equalsIgnoreCase("n/a") &&
	           !trimmed.equalsIgnoreCase("na") &&
	           !trimmed.equalsIgnoreCase("nil");
	}

	private boolean isNotEmpty(String value) {
	    return value != null && !value.trim().isEmpty();
	}
//	private List<BOQ> validateBOQData(List<BOQ> boqList, String projectId) {
//		Projects project = findByProjectId(projectId);
//		Iterator<BOQ> iterator = boqList.iterator();
//		while (iterator.hasNext()) {
//			BOQ boq = iterator.next();
//			BOQ savedBOQ = boqDao.findByBoqCodeAndProjectId(boq.getBoqCode(), projectId);
//			if (savedBOQ != null 
//					&& savedBOQ.getBoqName().equals(boq.getBoqName()) 
//					&& savedBOQ.getBoqCode().equals(boq.getBoqCode())) {
//				boq.setId(savedBOQ.getId());
//				boq.setParentBOQ(savedBOQ.getParentBOQ());
//				boq.setProject(project);
//			}
//		}
//		return boqList;
//	}
	@Override
	public List<BOQ> getParentBOQ(String projectId) {
		List<BOQ> boqList = boqDao.findByProjectId(projectId);
		if (boqList.isEmpty()) {
			return null;
		}
		List<BOQ> parentBoqList = boqList.stream()
				.filter(boq -> boq.getLevel() == 1)
				.collect(Collectors.toList());
		return parentBoqList;
	}
	@Override
	public List<BOQ> getChildBOQ(String projectId, String parentBoqId) {
		List<BOQ> boqList = boqDao.findByProjectIdAndParentBOQId(projectId, parentBoqId);
		if (boqList.isEmpty()) {
			return null;
		}
		return boqList;
	}
	@Override
	public Map<String, Integer> getBOQCount(String projectId) {
		List<BOQ> boqList = boqDao.findByProjectId(projectId);
		Map<String, Integer> boqCountMap = new HashMap<>();
		int boqcount = boqList.size();
		int lastLevelCount = (int) boqList.stream().filter(boq -> boq.isLastLevel()).count();
		boqCountMap.put("totalBOQCount", boqcount);
		boqCountMap.put("lastLevelBOQCount", lastLevelCount);
		return boqCountMap;
	}
	@Override
	public void deleteBOQ(List<String> boqCode, String projectId) throws DataIntegrityViolationException {
		for (String code : boqCode) {
			BOQ boq = boqDao.findById(code).orElseThrow(() -> new DataNotFoundException(
					messageSource.getMessage("boq.not.found", null, Locale.ENGLISH)));
			if (boq != null) {
				boqDao.delete(boq);
			}
		}
	}
	@Override
	public BOQ getBoqById(String boqId) {
		return boqDao.findById(boqId)
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("boq.not.found", null, Locale.ENGLISH)));
	}
	@Override
	public void generateBoqExcel(String projectId, HttpServletResponse response)
			throws IOException, DataNotFoundException {
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//		List<BOQ> boqList = getAllBOQ(projectId);
//		excelService.generateBoqFile(boqList, response);
	}
	@Override
	public void generateBoqPdf(String projectId, HttpServletResponse response)
			throws IOException, DocumentException, DataNotFoundException {
		response.setContentType("application/pdf");
//		List<BOQ> boqList = getAllBOQ(projectId);
//		pdfService.generateBoqPdf(boqList, response);
	}
	@Override
	public List<BOQ> getParentBOQ(List<BOQ> boqList) {
		if (boqList != null && !boqList.isEmpty()) {
			return boqList.stream().filter(boq -> boq.getParentBOQ() == null).collect(Collectors.toList());
		}
		return null;
	}
	@Override
	public double getParentTotalValue(List<BOQ> boqList, String parentCode) {
		double totalValue = 0.0;
		if (boqList != null) {
			for (BOQ boq : boqList) {
				if (boq.getParentBOQ() != null && boq.getParentBOQ().getBoqCode() != null
						&& boq.getParentBOQ().getBoqCode().contains(parentCode)) {
					totalValue += boq.getTotalAmount();
				}
			}
		}
		return totalValue;
	}
	@Override
	public String saveTemplate(Template template, Map<String, String> mapping) {
		if (templateDao.findByTemplateName(template.getTemplateName()) == null) {
			template.setId(UUID.randomUUID().toString());
			Template savedTemplate = templateDao.save(template);
			saveTemplateMapping(mapping, savedTemplate);
			return messageSource.getMessage("template.save.success", null, Locale.ENGLISH);
		}
		return messageSource.getMessage("template.duplicate", null, Locale.ENGLISH);
	}
	private void saveTemplateMapping(Map<String, String> mapping, Template savedTemplate) {
		for (Map.Entry<String, String> map : mapping.entrySet()) {
			TemplateMapping tempMap = new TemplateMapping();
			tempMap.setTemplate(savedTemplate);
			tempMap.setInternalFields(map.getKey());
			tempMap.setExcelFields(map.getValue());
			templateMappingDao.save(tempMap);
		}
	}
	@Override
	public List<Template> getAllTemplate() {
		List<Template> templates = templateDao.findAll();
		return templates;
	}
	@Override
	public TemplateDto getTemplateById(String templateId) {
		TemplateDto templateDto = new TemplateDto();
		Template template = templateDao.findById(templateId).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		templateDto.setTemplate(template);
		List<TemplateMapping> mappings = templateMappingDao.findByTemplateId(templateId);
		Map<String, String> mapping = new HashMap<>();
		for (TemplateMapping map : mappings) {
			mapping.put(map.getInternalFields(), map.getExcelFields());
		}
		templateDto.setTemplateMapping(mapping);
		return templateDto;
	}
	@Override
	public List<ProjectInfoDto> getAllBoqUploadedProject() {
		List<ProjectInfoDto> projects = new ArrayList<>();
		Set<String> projectIds = boqDao.findAll().stream().map(boq -> boq.getProject().getId())
				.collect(Collectors.toSet());
		for (String id : projectIds) {
			ProjectInfoDto projectInfo = projectDao.findAllProjectInfoById(id);
			if (projectInfo.isArchieve())
				continue;
			projects.add(projectInfo);
		}
		return projects;
	}
	@Override
	public String addActivityGroup(ActivityGroup activityGroup) {
		if (activityGroupsDao.findByActivityName(activityGroup.getActivityName()) == null) {
			activityGroup.setId(UUID.randomUUID().toString());
			activityGroupsDao.save(activityGroup);
			return messageSource.getMessage("activity.group.save.success", null, Locale.ENGLISH);
		}
		return messageSource.getMessage("activity.group.duplicate", null, Locale.ENGLISH);
	}
	@Override
	public List<ActivityGroup> getAllActivityGroups() {
		return activityGroupsDao.findAll();
	}
	@Override
	public String saveCostCodeOneToOne(List<CostCodeDto> dtos) {
		if (dtos == null || dtos.isEmpty()) {
			throw new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
		}
		Projects project = findByProjectId(dtos.get(0).getProjectId());
		for (CostCodeDto dto : dtos) {
			CostCodeActivity act = mapCommonFields(dto, project);
			act.setBoq(findBoqs(dto.getBoqId()));
			act.setQuantity(dto.getQuantity());
			act.setRate(dto.getRate());
			act.setAmount(dto.getQuantity() * dto.getRate());
			costCodeActivityDao.save(act);
		}
		return messageSource.getMessage("cost.code.save.success", null, Locale.ENGLISH);
	}
	@Override
	public String saveCostCodeOneToMany(List<CostCodeDto> dtos) {
		if (dtos == null || dtos.isEmpty()) {
			throw new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
		}
		Projects project = findByProjectId(dtos.get(0).getProjectId());
		for (CostCodeDto dto : dtos) {
			CostCodeActivity act = mapCommonFields(dto, project);
			Set<BOQ> boqSet = findBoqs(dto.getBoqId());
			act.setBoq(boqSet);
			if (dto.getSplitType() != null) {
				switch (dto.getSplitType().toLowerCase()) {
				case "quantity":
					double q = dto.getQuantity() * (dto.getQtySplitPercentage() / 100);
					act.setQuantity(q);
					act.setRate(dto.getRate());
					act.setAmount(q * dto.getRate());
					break;
				case "amount":
					double a = dto.getAmount() * (dto.getAmountSplitPercentage() / 100);
					act.setAmount(a);
					act.setRate(act.getAmount());
					act.setQuantity(1);
					for (UOM uom : uomDao.findAll()) {
						if (uom.getUomCode().equalsIgnoreCase("ls")) {
							act.setUom(uom);
							break;
						}
					}
					break;
				case "rate":
					double r = dto.getRate() * (dto.getRateSplitPercentage() / 100);
					act.setRate(r);
					act.setQuantity(dto.getQuantity());
					act.setAmount(r * dto.getQuantity());
					break;
				case "qty_rate":
					double qty = dto.getQuantity() * (dto.getQtySplitPercentage() / 100);
					double rate = dto.getRate() * (dto.getRateSplitPercentage() / 100);
					act.setQuantity(qty);
					act.setRate(rate);
					act.setAmount(qty * rate);
					break;
				}
			}
			costCodeActivityDao.save(act);
		}
		return messageSource.getMessage("cost.code.save.success", null, Locale.ENGLISH);
	}
	@Override
	public String saveCostCodeManyToOne(List<CostCodeDto> dtos) {
		if (dtos == null || dtos.isEmpty()) {
			throw new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
		}
		Projects project = findByProjectId(dtos.get(0).getProjectId());
		List<BOQ> allBoqs = dtos.stream().flatMap(d -> d.getBoqId().stream()).filter(id -> id != null)
				.map(id -> boqDao.findById(id)
						.orElseThrow(() -> new DataNotFoundException(
								messageSource.getMessage("data.not.found", null, Locale.ENGLISH))))
				.collect(Collectors.toList());
		if (allBoqs.isEmpty())
			throw new DataNotFoundException("No BOQs found for mapping");
		Set<String> uoms = allBoqs.stream().map(b -> b.getUom().getId()).collect(Collectors.toSet());
		double qty = allBoqs.stream().mapToDouble(BOQ::getQuantity).sum();
		double rate = allBoqs.stream().mapToDouble(BOQ::getTotalRate).average().orElse(0.0);
		double amount = qty * rate;
		CostCodeDto first = dtos.get(0);
		CostCodeActivity act = mapCommonFields(first, project);
		if (uoms.size() > 1) {
			for (UOM uom : uomDao.findAll()) {
				if (uom.getUomCode().equalsIgnoreCase("ls")) {
					act.setUom(uom);
					break;
				}
			}
		} else {
			act.setUom(allBoqs.get(0).getUom());
		}
		act.setBoq(new HashSet<>(allBoqs));
		act.setQuantity(qty);
		act.setRate(rate);
		act.setAmount(amount);
		costCodeActivityDao.save(act);
		return messageSource.getMessage("cost.code.save.success", null, Locale.ENGLISH);
	}
	private Set<BOQ> findBoqs(List<String> boqIds) {
		return boqIds.stream().filter(id -> id != null).map(id -> boqDao.findById(id).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH))))
				.collect(Collectors.toSet());
	}
	private CostCodeActivity mapCommonFields(CostCodeDto dto, Projects project) {
		CostCodeActivity costCodeAct = new CostCodeActivity();
		costCodeAct.setId(UUID.randomUUID().toString());
		costCodeAct.setProject(project);
		if (dto.getActivityGroupId() != null && !dto.getActivityGroupId().isEmpty()) {
			ActivityGroup activityGroup = activityGroupsDao.findById(dto.getActivityGroupId()).orElseThrow(
					() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
			costCodeAct.setActivityGroup(activityGroup);
		}
		if (dto.getCostCodeTypeId() != null && !dto.getCostCodeTypeId().isEmpty()) {
			CostCodeType costCodeType = costCodeTypeDao.findById(dto.getCostCodeTypeId()).orElseThrow(
					() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
			costCodeAct.setCostCodeType(costCodeType);
		}
		if (dto.getUomId() != null && !dto.getUomId().isEmpty()) {
			UOM uom = uomDao.findById(dto.getUomId()).orElseThrow(
					() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
			costCodeAct.setUom(uom);
		}
		costCodeAct.setActivityCode(dto.getActivityCode());
		costCodeAct.setActivityName(dto.getActivityName());
		costCodeAct.setMappingType(dto.getMappingType());
		return costCodeAct;
	}
	@Override
	public String deleteCostCode(String costCodeId) {
		CostCodeActivity costCode = costCodeActivityDao.findById(costCodeId).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		costCodeActivityDao.delete(costCode);
		return messageSource.getMessage("cost.code.delete.success", null, Locale.ENGLISH);
	}
	@Override
	public List<ProjectInfoDto> getAllCostCodeUpdatedProject() {
		List<ProjectInfoDto> projects = new ArrayList<>();
		Set<String> projectIds = costCodeActivityDao.findAll().stream().map(costCode -> costCode.getProject().getId())
				.collect(Collectors.toSet());
		for (String id : projectIds) {
			ProjectInfoDto projectInfo = projectDao.findAllProjectInfoById(id);
			if (projectInfo.isArchieve())
				continue;
			projects.add(projectInfo);
		}
		return projects;
	}
	@Override
	public List<CostCodeActivity> getCostCodeActivityByProject(String projectId) {
		return costCodeActivityDao.findByProjectId(projectId);
	}
	@Override
	public CostCodeActivity getCostCodeActivityById(String costCodeId) {
		return costCodeActivityDao.findById(costCodeId).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	}
	@Override
	public double getTotalAmountByCostCodeType(String costCodeTypeId, String projectId) {
		List<CostCodeActivity> activities = costCodeActivityDao.findByProjectIdAndCostCodeTypeId(projectId,
				costCodeTypeId);
		if (activities != null && !activities.isEmpty()) {
			return activities.stream().mapToDouble(CostCodeActivity::getAmount).sum();
		}
		return 0;
	}
	@Override
	public String addResourcesToTender(TenderResourceDto dto) {
		TenderEstimation tenderEst;
        if (dto.getId() != null) {
            tenderEst = tenderEstDao.findById(dto.getId()).orElse(new TenderEstimation());
            
        } else {
            tenderEst = new TenderEstimation();
            tenderEst.setId(UUID.randomUUID().toString()); 
        }       
        tenderEst.setDocNumber(dto.getDocNumber());
        tenderEst.setCoEfficient(dto.getCoEfficient());
        tenderEst.setWastePercentage(dto.getWastePercentage());
        tenderEst.setRate(dto.getRate());
        tenderEst.setAdditionalRate(dto.getAdditionalRate());
        tenderEst.setShippingPrice(dto.getShippingPrice());
        tenderEst.setCostUnitRate(dto.getCostUnitRate());
        tenderEst.setRateLock(dto.isRateLock());
        tenderEst.setExchangeRate(dto.getExchangeRate());
        tenderEst.setResourceType(dto.getResourceTypeId() != null 
        	    ? resourceTypeDao.findById(dto.getResourceTypeId()).orElse(null) 
        	    : null);

        	tenderEst.setResource(dto.getResourceId() != null
        	    ? resourceDao.findById(dto.getResourceId()).orElse(null)
        	    : null);
        	    
        	tenderEst.setUom(dto.getUomId() != null
        	    ? uomDao.findById(dto.getUomId()).orElse(null)
        	    : null);
        	    
        	tenderEst.setResourceNature(dto.getResourceNatureId() != null
        	    ? resourceNatureDao.findById(dto.getResourceNatureId()).orElse(null)
        	    : null);
        	    
        	tenderEst.setQuantityType(dto.getQuantityTypeId() != null
        	    ? quantityTypeDao.findById(dto.getQuantityTypeId()).orElse(null)
        	    : null);
        	    
        	tenderEst.setCurrency(dto.getCurrencyId() != null
        	    ? projectCurrencyDao.findById(dto.getCurrencyId()).orElse(null)
        	    : null);
        BOQ boq = new BOQ(); 
        boq.setId(dto.getBoqId()); 
        if (tenderEst.getBoq() == null) {
            tenderEst.setBoq(new HashSet<>());
        }
        tenderEst.getBoq().add(boq);
        tenderEst = tenderEstDao.save(tenderEst);
        TenderEstimationCostDetails costDetails = costDetailsDao
            .findByTenderEstimationIdAndBoqId(tenderEst.getId(), dto.getBoqId())
            .orElse(new TenderEstimationCostDetails());
        if (costDetails.getId() == null) {
             costDetails.setId(UUID.randomUUID().toString());
        }
        costDetails.setTenderEstimation(tenderEst);
        costDetails.setBoq(boq);
        costDetails.setCalculatedQuantity(dto.getCalculatedQuantity());
        costDetails.setWastePercentage(dto.getWastePercentage());
        costDetails.setWasteQuantity(dto.getWasteQuantity());
        costDetails.setNetQuantity(dto.getNetQuantity());
        costDetails.setRate(dto.getRate());
        costDetails.setAdditionalRate(dto.getAdditionalRate());
        costDetails.setShippingPrice(dto.getShippingPrice());
        costDetails.setCostUnitRate(dto.getCostUnitRate());
        costDetails.setExchangeRate(dto.getExchangeRate());
        costDetails.setTotalCostCompanyCurrency(dto.getTotalCostCompanyCurrency());
        costDetails.setResourceTotalCost(dto.getResourceTotalCost());
        costDetails.setCoEffSnapshot(dto.getCoEfficient()); 
        costDetails.setRateLock(dto.isRateLock());
        costDetailsDao.save(costDetails);
        BOQ boqEntity = boqDao.findById(dto.getBoqId()).orElse(null);
        if (boqEntity != null) {	
        	boqEntity.setTotalAmount(boqEntity.getTotalAmount() + dto.getTotalCostCompanyCurrency());
        	boqEntity.setTotalRate(boqEntity.getTotalAmount() / boqEntity.getQuantity());
			boqDao.save(boqEntity);
		}
        return messageSource.getMessage("tender.resource.save.success",null, Locale.ENGLISH);
	}
	
	@Override
	public String pasteResourceToBOQ(String boqId, List<String> tenderEstimationIds) {
		BOQ boq = boqDao.findById(boqId).orElse(null);
	    List<String> messages = new ArrayList<>();
	    for(String existingTenderEstId : tenderEstimationIds) {
	        TenderEstimation existingTenderEst = tenderEstDao.findById(existingTenderEstId).orElse(null);
	        if (existingTenderEst == null) {
	            messages.add("Failed to find existing TenderEstimation with ID: " + existingTenderEstId);
	            continue; 
	        }
	        TenderResourceDto dto = new TenderResourceDto();
	        dto.setResourceTypeId(existingTenderEst.getResourceType() != null ? existingTenderEst.getResourceType().getId() : null);
	        dto.setResourceId(existingTenderEst.getResource() != null ? existingTenderEst.getResource().getId() : null);
	        dto.setUomId(existingTenderEst.getUom() != null ? existingTenderEst.getUom().getId() : null);
	        dto.setResourceNatureId(existingTenderEst.getResourceNature() != null ? existingTenderEst.getResourceNature().getId() : null);
	        dto.setQuantityTypeId(existingTenderEst.getQuantityType() != null ? existingTenderEst.getQuantityType().getId() : null);
	        dto.setCurrencyId(existingTenderEst.getCurrency() != null ? existingTenderEst.getCurrency().getId() : null);
	        dto.setBoqId(boqId);
	        dto.setId(null); 
	        dto.setDocNumber(existingTenderEst.getDocNumber());
	        dto.setCoEfficient(existingTenderEst.getCoEfficient());
	        dto.setWastePercentage(existingTenderEst.getWastePercentage());
	        dto.setRate(existingTenderEst.getRate());
	        dto.setAdditionalRate(existingTenderEst.getAdditionalRate());
	        dto.setShippingPrice(existingTenderEst.getShippingPrice());
	        dto.setCostUnitRate(existingTenderEst.getCostUnitRate());
	        dto.setRateLock(existingTenderEst.isRateLock());
	        dto.setExchangeRate(existingTenderEst.getExchangeRate());	        
	        TenderEstimationCostDetails existingCostDetails = costDetailsDao
	             .findByTenderEstimationIdAndBoqId(existingTenderEstId, existingTenderEst.getBoq().iterator().next().getId())
	             .orElse(null);
	        if (existingCostDetails != null) {
	            dto.setCalculatedQuantity(boq.getQuantity() * existingCostDetails.getCoEffSnapshot());
	            dto.setWasteQuantity((existingCostDetails.getWastePercentage() / 100) * boq.getQuantity());
	            dto.setNetQuantity(dto.getCalculatedQuantity() + dto.getWasteQuantity());
	            dto.setTotalCostCompanyCurrency(dto.getNetQuantity() * existingCostDetails.getCostUnitRate());
	            dto.setResourceTotalCost(dto.getTotalCostCompanyCurrency() * dto.getExchangeRate());
	            dto.setCoEfficient(existingCostDetails.getCoEffSnapshot()); 
	        }
	        messages.add(addResourcesToTender(dto));
	    }
	    if(messages.size() == tenderEstimationIds.size()) {
	        return messageSource.getMessage("tender.resource.paste.success", null, Locale.ENGLISH);
	    }
	    return messageSource.getMessage("tender.resource.empty", null, Locale.ENGLISH);
	}
	@Override
	public ActivityGroup getActivityGroupById(String activityGroupId) {
		return activityGroupsDao.findById(activityGroupId).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	}
	@Override
	public List<TenderEstResourceDto> getEstimatedResourcesByBoq(String boqId) {
		List<TenderEstimation> estimations = tenderEstDao.findByBoqId(boqId);
		List<TenderEstResourceDto> dtoList = new ArrayList<>();
		for (TenderEstimation est : estimations) {
			TenderEstimationCostDetails costDetails = costDetailsDao
					.findByTenderEstimationIdAndBoqId(est.getId(), boqId)
					.orElse(null);
			TenderEstResourceDto dto = new TenderEstResourceDto();
			dto.setTenderEstimation(est);
			dto.setCostDetails(costDetails);
			dtoList.add(dto);
		}
		return dtoList;
	}
	@Override
	public String deleteTenderEstimationForBoq(String tenderEstimationId, String boqId) {
		TenderEstimation tenderEstimation = tenderEstDao.findById(tenderEstimationId).orElse(null);
		if (tenderEstimation != null) {
			Set<BOQ> boqSet = tenderEstimation.getBoq();
			boqSet.removeIf(boq -> boq.getId().equals(boqId));
			if (boqSet.isEmpty()) {
				TenderEstimationCostDetails costDetails = costDetailsDao.findByTenderEstimationId(tenderEstimationId);
				if (costDetails != null) {
					costDetailsDao.delete(costDetails);
				}
				tenderEstDao.delete(tenderEstimation);
			} else {
				tenderEstimation.setBoq(boqSet);
				tenderEstDao.save(tenderEstimation);
				Optional<TenderEstimationCostDetails> costDetails = costDetailsDao.findByTenderEstimationIdAndBoqId(tenderEstimationId, boqId);
				costDetails.ifPresent(costDetailsDao::delete);
			}
			return messageSource.getMessage("tender.resource.delete.success", null, Locale.ENGLISH);
		}
		return null;
	}
	@Override
	public TenderEstResourceDto getEstimatedResourceById(String tenderEstimationId) {
		TenderEstimation tenderEstimation = tenderEstDao.findById(tenderEstimationId).orElse(null);
		if (tenderEstimation != null) {
			TenderEstimationCostDetails costDetails = costDetailsDao.findByTenderEstimationId(tenderEstimationId);
			TenderEstResourceDto dto = new TenderEstResourceDto();
			dto.setTenderEstimation(tenderEstimation);
			dto.setCostDetails(costDetails);
			return dto;
		}
		return null;
	}
}