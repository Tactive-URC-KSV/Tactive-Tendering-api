package com.ksv.tactive.service.implmn;

import java.io.IOException;
import java.io.InputStream;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.DocumentException;
import com.ksv.tactive.dto.CostCodeDto;
import com.ksv.tactive.dto.FeasibilityApprovalDocuDTO;
import com.ksv.tactive.dto.ProjectInfoDto;
import com.ksv.tactive.dto.ProjectInputDTO;
import com.ksv.tactive.dto.TemplateDto;
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
import com.ksv.tactive.project.dao.ProjectDao;
import com.ksv.tactive.project.dao.ProjectDocumentDetailsDao;
import com.ksv.tactive.project.dao.ProjectStatusDao;
import com.ksv.tactive.project.dao.RegionDao;
import com.ksv.tactive.project.dao.ScopeOfPackDao;
import com.ksv.tactive.project.dao.SectorDao;
import com.ksv.tactive.project.dao.TechnicalFeasibilityDao;
import com.ksv.tactive.project.dao.TemplateDao;
import com.ksv.tactive.project.dao.TemplateMappingDao;
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
import com.ksv.tactive.project.entity.UOM;
import com.ksv.tactive.service.ProjectService;
import com.ksv.tactive.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class ProjectServiceImpl implements ProjectService{
	
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
	
	@Override
	public List<ProjectInfoDto> getAllProjects() {
		List<ProjectInfoDto> projects = projectDao.findAllProjectInfo().stream()
				.filter(project -> !project.isArchieve())
				.collect(Collectors.toList());
		return projects;
	}

	@Override
	public Projects findByProjectId(String projectId) {
		return projectDao.findById(projectId)
				.orElseThrow(() -> new ProjectNotFoundException(messageSource.getMessage("project.not.found", null, Locale.ENGLISH)));
	}
	
	@Override
	public Projects saveProject(ProjectInputDTO projectInputDTO, HttpServletRequest request) throws SQLIntegrityConstraintViolationException {
		
		Projects project = projectInputDTO.getProject();
		if(projectDao.findByProjectCode(project.getProjectCode()) != null) {
			throw new DuplicateProjectCodeException(messageSource.getMessage("project.code.duplicate", null , Locale.ENGLISH));
		}
		
		project.setId(UUID.randomUUID().toString());
		project.setProjectCode(generateProjectCode());
		if(project.getProjectName() == null || project.getShortName() == null ) {
			throw new DataNotFoundException(messageSource.getMessage("project.name.not.found", null, Locale.ENGLISH));
		}
		
		project.setEstimatedValue(project.getBuildingArea() * project.getRatePerUnit());
		if (!projectInputDTO.getRegionId().isEmpty()) {
		    project.setRegion(
		        regionRepo.findById(projectInputDTO.getRegionId())
		            .orElseThrow(() -> new DataNotFoundException(
		                messageSource.getMessage("data.not.found", null, Locale.ENGLISH))));
		}
		if(!projectInputDTO.getSectorId().isEmpty()) {
			project.setSector(sectorDao.findById(projectInputDTO.getSectorId())
					.orElseThrow(() -> new DataNotFoundException(
							messageSource.getMessage("data.not.found", null, Locale.ENGLISH))));
		}
		if(!projectInputDTO.getUomId().isEmpty()) {
			project.setUom(uomDao.findById(projectInputDTO.getUomId())
					.orElseThrow(() -> new DataNotFoundException(
							messageSource.getMessage("data.not.found", null, Locale.ENGLISH))));
		}
		if(!projectInputDTO.getScopeOfPackageIds().isEmpty()) {
			Set<ScopeOfPackage> scopePackages = projectInputDTO.getScopeOfPackageIds().stream()
					.filter(id -> id != null && !id.trim().isEmpty())
				    .map(id -> scopeOfPackDao.findById(id)
				    		.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH))))
				    .collect(Collectors.toSet());
			project.setScopeOfPackages(scopePackages);
		}
		project.setUpdateAt(Date.valueOf(LocalDate.now()));
		project.setCreatedAt(Date.valueOf(LocalDate.now()));
		project.setCreatedBy(userService.findLoggedInUser().getId());
		project.setCompanyId(userService.findLoggedInUser().getCompanyId());
		project.setProjectStatus(statusDao.findByProcessUrl(request.getRequestURI()));
		Projects savedProject = projectDao.save(project);
		
		if(savedProject != null) {
			return savedProject;
		}
		throw new SQLIntegrityConstraintViolationException(messageSource.getMessage("project.created.exception", null, Locale.ENGLISH));
	}
	
	private String generateProjectCode() {
		final String chars = "0123456789";
		Random random = new Random();
		StringBuilder code = new StringBuilder();
		for(int i=0; i<3; i++) {
			int index = random.nextInt(chars.length());
            code.append(chars.charAt(index));
		}
		return "PRJ - "+code.toString();
	}

	@Override
	public String saveProjectFiles(String projectId, List<MultipartFile> files) {
		Projects project = findByProjectId(projectId);
		Path directory=Paths.get(uploadDir,
				project.getProjectName()
				.trim()
				.replaceAll("\\s+", "_")                    
			    .replaceAll("[^a-zA-Z0-9-_\\.]", ""))
				.toAbsolutePath().normalize();
		try {
			Files.createDirectories(directory);
			for(MultipartFile file : files) {
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
		if(existingProject == null) {
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
		    project.setRegion(
		        regionRepo.findById(projectInputDTO.getRegionId())
		            .orElseThrow(() -> new DataNotFoundException(
		                messageSource.getMessage("data.not.found", null, Locale.ENGLISH))));
		}
		if(!projectInputDTO.getSectorId().isEmpty()) {
			project.setSector(sectorDao.findById(projectInputDTO.getSectorId())
					.orElseThrow(() -> new DataNotFoundException(
							messageSource.getMessage("data.not.found", null, Locale.ENGLISH))));
		}
		if(!projectInputDTO.getUomId().isEmpty()) {
			project.setUom(uomDao.findById(projectInputDTO.getUomId())
					.orElseThrow(() -> new DataNotFoundException(
							messageSource.getMessage("data.not.found", null, Locale.ENGLISH))));
		}
		if(!projectInputDTO.getScopeOfPackageIds().isEmpty()) {
			Set<ScopeOfPackage> scopePackages = projectInputDTO.getScopeOfPackageIds().stream()
					.filter(id -> id != null && !id.trim().isEmpty())
				    .map(id -> scopeOfPackDao.findById(id)
				    		.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH))))
				    .collect(Collectors.toSet());
			project.setScopeOfPackages(scopePackages);
		}
		project.setUpdateAt(Date.valueOf(LocalDate.now()));
		projectDao.save(project);
		
	}
	
	@Override
	public String deleteProject(String projectId) {
		Projects project=findByProjectId(projectId);
		project.setArchieve(true);
		if(project!=null) {
			projectDao.save(project);
		}
		return messageSource.getMessage("project.delete.success", null , Locale.ENGLISH);
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
	    	
	       for(Map.Entry<String, MultipartFile> entry : approvalDoc.entrySet()) {
	    	   
	    	   String key =entry.getKey();
	    	   
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
	                throw new FileNotSaveException(messageSource.getMessage("file.upload.failure", null, Locale.ENGLISH));
	            }

	            ApprovalDocuments approvalDocuments = new ApprovalDocuments();
	            approvalDocuments.setId(UUID.randomUUID().toString());

	            for (FeasibilityApprovalDocuDTO dto : approvalDocuDTO) {
	                if (dto.getDocumentName().equalsIgnoreCase(doc)) {
	                    approvalDocuments.setDocumentName(dto.getDocumentName());
	                    approvalDocuments.setComments(dto.getComment());
	                    System.err.println(dto.isApproved());
	                    approvalDocuments.setApproved(dto.isApproved());
	                    break; 
	                }
	            }

	            approvalDocuments.setFilePath(filePath);
	            approvalDocuments.setTechnicalFeasibility(
	                    techFeaDao.findById(techFeasibilityId)
	                            .orElseThrow(() -> new DataNotFoundException(
	                                    messageSource.getMessage("data.not.found", null, Locale.ENGLISH)))
	            );

	            lastSavedId = approvalsDao.save(approvalDocuments).getId();
	        }

	        return lastSavedId; 
	    }

	    return null;
	}

	
	private String saveApprovalDocFile(MultipartFile file, String projectId) {
		String projectName = findByProjectId(projectId).getProjectName();
		Path directory=Paths.get(uploadDir,
				projectName
				.trim()
				.replaceAll("\\s+", "_")                    
			    .replaceAll("[^a-zA-Z0-9-_\\.]", ""))
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
		TechnicalFeasibility techFeasibility = techFeaDao.findById(techFeasibilityId)
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		FinancialFeasibility finFeasibility = finFeasibilityDao.findById(finFeasibilityId)
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		if(project != null && techFeasibility != null && finFeasibility != null) {
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
		return feaStudyDao.findByProjectId(projectId)
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	}
	
	@Override
	public List<ApprovalDocuments> getApprovalDocData(String techId) {
		List<ApprovalDocuments> approvalDoc = approvalsDao.findAllByTechnicalFeasibilityId(techId);
		return approvalDoc;
	}
	
	@Override
	public void saveProjectDrawings(String projectId, List<MultipartFile> drawings) {
		Projects project = findByProjectId(projectId);
		Path directory=Paths.get(uploadDir,
				project.getProjectName()
				.trim()
				.replaceAll("\\s+", "_")                    
			    .replaceAll("[^a-zA-Z0-9-_\\.]", ""))
				.toAbsolutePath().normalize();
		Path targetDir = directory.resolve("drawings");
		try {
			Files.createDirectories(targetDir);
			for(MultipartFile file : drawings) {
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
	
//	@Override
//	public Map<ApprovalDocuments,Resource> getApprovalDocs(List<ApprovalDocuments> approvalDoc) {
//		Map<ApprovalDocuments, Resource> approvalDocMap = new HashMap<>();
//		for (ApprovalDocuments doc : approvalDoc) {
//			try {
//				Path filePath = Paths.get(doc.getFilePath());
//				Resource resource = new UrlResource(filePath.toUri());
//				if (resource.exists() || resource.isReadable()) {
//					approvalDocMap.put(doc, resource);
//				} else {
//					throw new FileNotSaveException(messageSource.getMessage("file.not.found", null, Locale.ENGLISH));
//				}
//			} catch (MalformedURLException e) {
//				e.printStackTrace();
//			}
//			
//		}
//		return approvalDocMap;
//	}

	@Override
	public List<ProjectInfoDto> getAllApprovedProjects() {
		List<String> projectId = feaStudyDao.findAll().stream()
				.filter(id -> id.isFeasibilityApproved())
				.map(project -> project.getProject().getId())
				.collect(Collectors.toList());
		List<ProjectInfoDto> approvedProjects = projectId.stream()
				.map(id -> projectDao.findAllProjectInfoById(id))
				.filter(project -> !project.isArchieve())
				.collect(Collectors.toList());
		return approvedProjects.stream()
				.filter(project -> project !=null)
				.collect(Collectors.toList());
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
		if(fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
			return excelService.getColumns(boqFile, sheetName);
		}
		return null;
	}
	
	@Override
	public String saveBoqFile(MultipartFile boqFile, String projectId) {
		String projectName = findByProjectId(projectId).getProjectName();
		Path directory = Paths.get(uploadDir,
		        projectName.trim()
		                   .replaceAll("\\s+", "_")
		                   .replaceAll("[^a-zA-Z0-9-_\\.]", ""))
		        .toAbsolutePath().normalize()
		        .resolve("BOQ");

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
	
	@Override
	public String mapBOQ(String projectId, String sheetName, Map<String, String> columnMapping, String filePath) throws IOException {
		Projects project = findByProjectId(projectId);
		Resource resource = new UrlResource(Paths.get(filePath).toUri());
		List<BOQ> boqList = null;
		if(resource.exists() || resource.isReadable()) {
			String fileName = resource.getFilename();
			InputStream inputStream = resource.getInputStream();
			if(fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
				boqList = excelService.mapBOQData(sheetName, columnMapping, inputStream); 
			}
			else if(fileName.endsWith(".pdf")) {
				boqList = pdfService.mapBOQData(columnMapping, inputStream);
			}
			else {
				throw new FileNotSaveException(messageSource.getMessage("file.type.not.supported", null, Locale.ENGLISH));
			}
			boqList = validateBOQData(boqList,projectId);
			if(!boqList.isEmpty()) {
				for(BOQ boq : boqList) {
					boq.setProject(project);
					if(boq.getBoqCode().length() > 1) {
						String parentCode = boq.getBoqCode().substring(0, boq.getBoqCode().length() - 2);
						BOQ parentBoq = boqDao.findByBoqCodeAndProjectId(parentCode, projectId);
						boq.setParentBOQ(parentBoq);
					}
					boqDao.save(boq);
				}
			}
			
			return "BOQ data mapped successfully";
		}
		return null;
	}
	private List<BOQ> validateBOQData(List<BOQ> boqList, String projectId) {
		Projects project = findByProjectId(projectId);
		Iterator<BOQ> iterator = boqList.iterator();
		while(iterator.hasNext()) {
			BOQ boq = iterator.next();
			BOQ savedBOQ = boqDao.findByBoqCodeAndProjectId(boq.getBoqCode(), projectId);
			if(savedBOQ != null && savedBOQ.getBoqName().equals(boq.getBoqName())) {
				boq.setId(savedBOQ.getId());
				boq.setParentBOQ(savedBOQ.getParentBOQ());
				boq.setProject(project);
                boqDao.save(boq);
                iterator.remove();
			}
			
		}
		return boqList;
	}

	
	@Override
	public List<BOQ> getAllBOQ(String projectId) {
		List<BOQ> boqList = boqDao.findByProjectId(projectId);
		if(boqList.isEmpty()) {
			throw new DataNotFoundException(messageSource.getMessage("boq.not.found", null, Locale.ENGLISH));
		}
		return boqList;
	}
	
	@Override
	public void deleteBOQ(List<String> boqCode, String projectId) {
		for(String code : boqCode) {
			BOQ boq = boqDao.findByBoqCodeAndProjectId(code, projectId);
			if(boq != null) {
				boqDao.delete(boq);
			}
		}
	}
	
	@Override
	public void generateBoqExcel(String projectId, HttpServletResponse response) throws IOException , DataNotFoundException{
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		List<BOQ> boqList = getAllBOQ(projectId);
		excelService.generateBoqFile(boqList,response);
		
	}
	
	@Override
	public void generateBoqPdf(String projectId, HttpServletResponse response) throws IOException, DocumentException, DataNotFoundException {
		response.setContentType("application/pdf");	
		List<BOQ> boqList = getAllBOQ(projectId);
		pdfService.generateBoqPdf(boqList, response);
	}
	
	@Override
	public List<BOQ> getParentBOQ(List<BOQ> boqList) {
		if(boqList != null && !boqList.isEmpty()) {
			return boqList.stream()
					.filter(boq -> boq.getParentBOQ() == null )
					.collect(Collectors.toList());
		}
		return null;
	}
	@Override
	public double getParentTotalValue(List<BOQ> boqList, String parentCode) {
		double totalValue = 0.0;
		if(boqList != null) {
			for(BOQ boq : boqList) {
				if(boq.getParentBOQ() != null && 
						boq.getParentBOQ().getBoqCode() != null && 
						boq.getParentBOQ().getBoqCode().contains(parentCode)) {
					totalValue += boq.getTotalAmount(); 
				}
			}
		}
		return totalValue;
	}
	
	@Override
	public String saveTemplate(Template template, Map<String,String> mapping) {
		if(templateDao.findByTemplateName(template.getTemplateName()) == null) {
			template.setId(UUID.randomUUID().toString());
			Template savedTemplate = templateDao.save(template);
			saveTemplateMapping(mapping, savedTemplate);
			return messageSource.getMessage("template.save.success", null, Locale.ENGLISH);
		}
		return messageSource.getMessage("template.duplicate", null, Locale.ENGLISH);
	}

	private void saveTemplateMapping(Map<String, String> mapping, Template savedTemplate) {
		for(Map.Entry<String, String> map : mapping.entrySet()) {
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
		Template template = templateDao.findById(templateId)
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		templateDto.setTemplate(template);
		List<TemplateMapping> mappings = templateMappingDao.findByTemplateId(templateId);
		Map<String, String> mapping = new HashMap<>();
		for(TemplateMapping map : mappings) {
			mapping.put(map.getInternalFields(), map.getExcelFields());
		}
		templateDto.setTemplateMapping(mapping);
		return templateDto;
	}
	
	@Override
	public List<ProjectInfoDto> getAllBoqUploadedProject() {
		List<ProjectInfoDto> projects = new ArrayList<>();
		Set<String> projectIds = boqFileDao.findAll().stream()
				.map(boq -> boq.getProject().getId())
				.collect(Collectors.toSet());
		for(String id : projectIds) {
			ProjectInfoDto projectInfo = projectDao.findAllProjectInfoById(id);
			if(projectInfo.isArchieve()) continue;
			projects.add(projectInfo);
		}
		return projects;
	}
	
	
	@Override
	public String addActivityGroup(ActivityGroup activityGroup) {
		if(activityGroupsDao.findByActivityName(activityGroup.getActivityName()) == null) {
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
	public String saveCostCodeOneToOne(List<CostCodeDto> costCodeDto) {
		if(costCodeDto == null || costCodeDto.isEmpty()) {
			throw new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
		}
		Projects project = findByProjectId(costCodeDto.get(0).getProjectId());
		for(CostCodeDto costCode : costCodeDto) {
			CostCodeActivity costCodeAct = new CostCodeActivity();
			costCodeAct.setId(UUID.randomUUID().toString());
			if(!costCode.getActivityGroupId().isEmpty()) {
				ActivityGroup activityGroup = activityGroupsDao.findById(costCode.getActivityGroupId())
						.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
				costCodeAct.setActivityGroup(activityGroup);
			}
			if(!costCode.getCostCodeTypeId().isEmpty()) {
				CostCodeType costCodeType = costCodeTypeDao.findById(costCode.getCostCodeTypeId())
						.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
				costCodeAct.setCostCodeType(costCodeType);
			}
			if(!costCode.getUomId().isEmpty()) {
				UOM uom = uomDao.findById(costCode.getUomId())
						.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
				costCodeAct.setUom(uom);
			}
			Set<BOQ> boqList = costCode.getBoqId().stream()
					.filter(id -> id != 0 )
				    .map(id -> boqDao.findById(id)
				    		.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH))))
				    .collect(Collectors.toSet());
			costCodeAct.setBoq(boqList);
			costCodeAct.setProject(project);
			costCodeAct.setActivityCode(costCode.getActivityCode());
			costCodeAct.setActivityName(costCode.getActivityName());
			costCodeAct.setMappingType(costCode.getMappingType());
			costCodeAct.setQuantity(costCode.getQuantity());
			costCodeAct.setRate(costCode.getRate());
			costCodeAct.setAmount(costCode.getQuantity() * costCode.getRate());
			costCodeActivityDao.save(costCodeAct);
			
		}
		return messageSource.getMessage("cost.code.save.success", null, Locale.ENGLISH);
	}
	
	@Override
	public String saveCostCodeOneToMany(List<CostCodeDto> costCodeDto) {
		if (costCodeDto == null || costCodeDto.isEmpty()) {
	        throw new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
	    }

	    Projects project = findByProjectId(costCodeDto.get(0).getProjectId());

	    for (CostCodeDto dto : costCodeDto) {
	        CostCodeActivity costCodeAct = new CostCodeActivity();
	        costCodeAct.setId(UUID.randomUUID().toString());

	        if (dto.getActivityGroupId() != null && !dto.getActivityGroupId().isEmpty()) {
	            ActivityGroup activityGroup = activityGroupsDao.findById(dto.getActivityGroupId())
	                    .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	            costCodeAct.setActivityGroup(activityGroup);
	        }

	        if (dto.getCostCodeTypeId() != null && !dto.getCostCodeTypeId().isEmpty()) {
	            CostCodeType costCodeType = costCodeTypeDao.findById(dto.getCostCodeTypeId())
	                    .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	            costCodeAct.setCostCodeType(costCodeType);
	        }

	        if (dto.getUomId() != null && !dto.getUomId().isEmpty()) {
	            UOM uom = uomDao.findById(dto.getUomId())
	                    .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	            costCodeAct.setUom(uom);
	        }

	        Set<BOQ> boqSet = dto.getBoqId().stream()
	                .filter(id -> id != 0)
	                .map(id -> boqDao.findById(id)
	                        .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH))))
	                .collect(Collectors.toSet());

	        costCodeAct.setBoq(boqSet);
	        costCodeAct.setProject(project);
	        costCodeAct.setActivityCode(dto.getActivityCode());
	        costCodeAct.setActivityName(dto.getActivityName());
	        costCodeAct.setMappingType(dto.getMappingType());

	        if (dto.getSplitType() != null) {
	            int boqCount = boqSet.size();

	            switch (dto.getSplitType().toLowerCase()) {
	                case "quantity": {
	                    double splitQty = dto.getQuantity() / boqCount;
	                    costCodeAct.setQuantity(splitQty);
	                    costCodeAct.setRate(dto.getRate());
	                    costCodeAct.setAmount(splitQty * dto.getRate());
	                    break;
	                }
	                case "amount": {
	                    double splitAmount = dto.getAmount() / boqCount;
	                    costCodeAct.setAmount(splitAmount);
	                    costCodeAct.setRate(dto.getRate());
	                    double derivedQty = splitAmount / dto.getRate();
	                    costCodeAct.setQuantity(derivedQty);
	                    break;
	                }
	                case "rate": {
	                    double splitRate = dto.getRate() / boqCount;
	                    costCodeAct.setRate(splitRate);
	                    costCodeAct.setAmount(dto.getAmount());
	                    double derivedQty = dto.getAmount() / splitRate;
	                    costCodeAct.setQuantity(derivedQty);
	                    break;
	                }
	                default:
	                    throw new IllegalArgumentException("Invalid split type: " + dto.getSplitType());
	            }
	        }
	        costCodeActivityDao.save(costCodeAct);
	    }

	    return messageSource.getMessage("cost.code.save.success", null, Locale.ENGLISH);
	}
	
	@Override
	public String saveCostCodeManyToOne(List<CostCodeDto> costCodeDto) {
		if (costCodeDto == null || costCodeDto.isEmpty()) {
	        throw new DataNotFoundException(
	            messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
	        );
	    }

	    Projects project = findByProjectId(costCodeDto.get(0).getProjectId());

	    List<BOQ> allBoqs = costCodeDto.stream()
	            .flatMap(dto -> dto.getBoqId().stream())
	            .filter(id -> id != 0)
	            .map(id -> boqDao.findById(id)
	                    .orElseThrow(() -> new DataNotFoundException(
	                        messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
	                    )))
	            .collect(Collectors.toList());

	    if (allBoqs.isEmpty()) {
	        throw new DataNotFoundException("No BOQs found for mapping");
	    }

	    Set<String> uomIds = allBoqs.stream()
	            .map(b -> b.getUom().getId())
	            .collect(Collectors.toSet());

	    if (uomIds.size() > 1) {
	        throw new IllegalArgumentException("All BOQs must have the same UOM for many-to-one mapping");
	    }

	    double totalQty = allBoqs.stream()
	            .mapToDouble(BOQ::getQuantity)
	            .sum();

	    double avgRate = allBoqs.stream()
	            .mapToDouble(BOQ::getTotalRate)
	            .average()
	            .orElse(0.0);

	    double totalAmount = totalQty * avgRate;

	    UOM commonUom = allBoqs.get(0).getUom();

	    CostCodeActivity costCodeAct = new CostCodeActivity();
	    costCodeAct.setId(UUID.randomUUID().toString());

	    CostCodeDto firstDto = costCodeDto.get(0);

	    if (firstDto.getActivityGroupId() != null && !firstDto.getActivityGroupId().isEmpty()) {
	        ActivityGroup activityGroup = activityGroupsDao.findById(firstDto.getActivityGroupId())
	                .orElseThrow(() -> new DataNotFoundException(
	                    messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
	                ));
	        costCodeAct.setActivityGroup(activityGroup);
	    }

	    if (firstDto.getCostCodeTypeId() != null && !firstDto.getCostCodeTypeId().isEmpty()) {
	        CostCodeType costCodeType = costCodeTypeDao.findById(firstDto.getCostCodeTypeId())
	                .orElseThrow(() -> new DataNotFoundException(
	                    messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
	                ));
	        costCodeAct.setCostCodeType(costCodeType);
	    }

	    
	    costCodeAct.setUom(commonUom);
	    costCodeAct.setQuantity(totalQty);
	    costCodeAct.setRate(avgRate);
	    costCodeAct.setAmount(totalAmount);

	    costCodeAct.setBoq(new HashSet<>(allBoqs));
	    costCodeAct.setProject(project);
	    costCodeAct.setActivityCode(firstDto.getActivityCode());
	    costCodeAct.setActivityName(firstDto.getActivityName());
	    costCodeAct.setMappingType(firstDto.getMappingType());
	    
	    costCodeActivityDao.save(costCodeAct);

	    return messageSource.getMessage("cost.code.save.success", null, Locale.ENGLISH);
	}
	
	@Override
	public String deleteCostCode(String costCodeId) {
		CostCodeActivity costCode = costCodeActivityDao.findById(costCodeId)
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		costCodeActivityDao.delete(costCode);
		return messageSource.getMessage("cost.code.delete.success", null, Locale.ENGLISH);
	}
	
	@Override
	public List<CostCodeActivity> getCostCodeActivityByType(String projectId) {
		return costCodeActivityDao.findByProjectId(projectId);
	}
}
