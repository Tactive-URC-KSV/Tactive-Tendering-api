package com.ksv.tactive.service.implmn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.HashMap;
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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ksv.tactive.dto.FeasibilityApprovalDocuDTO;
import com.ksv.tactive.dto.ProjectInfoDto;
import com.ksv.tactive.dto.ProjectInputDTO;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.exception.DuplicateEntryException;
import com.ksv.tactive.exception.DuplicateProjectCodeException;
import com.ksv.tactive.exception.FileNotSaveException;
import com.ksv.tactive.exception.ProjectNotFoundException;
import com.ksv.tactive.project.dao.ApprovalsDao;
import com.ksv.tactive.project.dao.FeasibilityStudyDao;
import com.ksv.tactive.project.dao.FinancialFeasibilityDao;
import com.ksv.tactive.project.dao.ListOfApprovalsDao;
import com.ksv.tactive.project.dao.ProjectDao;
import com.ksv.tactive.project.dao.ProjectDocumentDetailsDao;
import com.ksv.tactive.project.dao.ProjectStatusDao;
import com.ksv.tactive.project.dao.RegionDao;
import com.ksv.tactive.project.dao.ScopeOfPackDao;
import com.ksv.tactive.project.dao.SectorDao;
import com.ksv.tactive.project.dao.TechnicalFeasibilityDao;
import com.ksv.tactive.project.dao.UOMDao;
import com.ksv.tactive.project.entity.ApprovalDocuments;
import com.ksv.tactive.project.entity.FeasibilityStudy;
import com.ksv.tactive.project.entity.FinancialFeasibility;
import com.ksv.tactive.project.entity.ListOfApprovals;
import com.ksv.tactive.project.entity.ProjectDocumentDetails;
import com.ksv.tactive.project.entity.ProjectStatus;
import com.ksv.tactive.project.entity.Projects;
import com.ksv.tactive.project.entity.Region;
import com.ksv.tactive.project.entity.ScopeOfPackage;
import com.ksv.tactive.project.entity.Sector;
import com.ksv.tactive.project.entity.TechnicalFeasibility;
import com.ksv.tactive.project.entity.UOM;
import com.ksv.tactive.service.ProjectService;
import com.ksv.tactive.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ProjectServiceImpl implements ProjectService{
	
	@Value("${file.upload.path}")
	private String uploadDir;
	
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
	private ListOfApprovalsDao listOfApprovalsDao;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private ProjectStatusDao statusDao;
	
	@Autowired
	private ProjectStatusDao projectStatusDao;
	
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

	@Override
	public String addSector(Sector sector) {
		sector.setId(UUID.randomUUID().toString());
		sectorDao.save(sector);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}

	@Override
	public String addScope(ScopeOfPackage scope) {
		scope.setId(UUID.randomUUID().toString());
		scopeOfPackDao.save(scope);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}

	@Override
	public String addUom(UOM uom) {
		uom.setId(UUID.randomUUID().toString());
		uomDao.save(uom);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}
	@Override
	public List<Region> getAllRegions() {
		return regionRepo.findAll();
	}

	@Override
	public String addRegion(Region region) {
		if(regionRepo.findByRegionName(region.getRegionName()) != null) {
			throw new DuplicateEntryException(messageSource.getMessage("region.duplicate", null, Locale.ENGLISH));
		}
		region.setId(UUID.randomUUID().toString());
		regionRepo.save(region);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}

	@Override
	public List<UOM> getAllUOMs() {
		return uomDao.findAll();
	}

	@Override
	public List<Sector> getAllSector() {
		return sectorDao.findAll();
	}

	@Override
	public List<ScopeOfPackage> getAllScopes() {
		return scopeOfPackDao.findAll();
	}

	@Override
	public List<ProjectStatus> getAllProjectStatus() {
		return projectStatusDao.findAll();
	}

	@Override
	public String addProjectStatus(ProjectStatus projectStatus) {
		projectStatus.setId(UUID.randomUUID().toString());
		projectStatusDao.save(projectStatus);
		return null;
	}

	
	@Override
	public List<ProjectInfoDto> getAllProjects() {
		return projectDao.findAllProjectInfo();
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
		project.setCreatedAt(Date.valueOf(LocalDate.now()));
		project.setUpdateAt(Date.valueOf(LocalDate.now()));
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
		StringBuilder password = new StringBuilder();
		for(int i=0; i<3; i++) {
			int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
		}
		return password.toString();
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
	public String deleteProject(String projectId) {
		Projects project=findByProjectId(projectId);
		if(project!=null) {
			projectDao.delete(project);
			return messageSource.getMessage("project.delete.success", null , Locale.ENGLISH);
		}
		return null;
	}

	@Override
	public String addListOfApprovals(ListOfApprovals listOfApprovals) {
		listOfApprovals.setId(UUID.randomUUID().toString());
		listOfApprovalsDao.save(listOfApprovals);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}

	@Override
	public List<ListOfApprovals> getAllListOfApprovals() {
		List<ListOfApprovals> listOfApprovals = listOfApprovalsDao.findAll();
		return listOfApprovals;
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
	public String updateFeasibilityStudyStatus(String projectId, boolean status, String comments, HttpServletRequest request) {
		FeasibilityStudy feasibilityStudy = feaStudyDao.findByProjectId(projectId)
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		if(feasibilityStudy != null) {
			feasibilityStudy.setFeasibilityApproved(status);
			feasibilityStudy.setComments(comments);
			feasibilityStudy.setReviewedBy(userService.findLoggedInUser().getId());
			feaStudyDao.save(feasibilityStudy);
			return messageSource.getMessage("feasibility.study.status.update.success", null, Locale.ENGLISH);
		}
		return null;
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

}
