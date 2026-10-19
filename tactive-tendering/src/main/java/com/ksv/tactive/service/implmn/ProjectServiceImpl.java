package com.ksv.tactive.service.implmn;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.ksv.tactive.dataloader.LocationMemoryEngine;
import com.ksv.tactive.dto.*;
import com.ksv.tactive.exception.DuplicateEntryException;
import com.ksv.tactive.exception.ProjectNotFoundException;
import com.ksv.tactive.project.dao.*;
import com.ksv.tactive.project.entity.*;
import com.ksv.tactive.project.entity.Currency;
import com.ksv.tactive.service.*;
import com.ksv.tactive.user.entity.User;
import com.ksv.tactive.util.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.DocumentException;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.exception.FileNotSaveException;
import com.ksv.tactive.fileservice.ExcelFileService;
import com.ksv.tactive.fileservice.PdfFileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {
	@Value("${file.upload.path}")
	private String uploadDir;
	@Autowired
	private MasterService masterService;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private ContractorService contractorService;
	@Autowired
	private ProjectDao projectDao;
	@Autowired
	private UserService userService;
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
	private TenderEstimationDao tenderEstDao;
	@Autowired
	private ActivityGroupDao activityGroupsDao;
	@Autowired
	private CostCodeTypeDao costCodeTypeDao;
	@Autowired
	private CostCodeDao costCodeActivityDao;
	@Autowired
	private TenderDao tenderDao;
	@Autowired
	private TenderAttachmentDao tenderAttachmentDao;
	@Autowired
	private TenderEditHistoryDao tenderEditHistoryDao;
	@Autowired
	private EmailService emailService;
	@Autowired
	private TenderInviteDao tenderInviteDao;
	@Autowired
	private TenderOffersDao tenderOffersDao;
	@Autowired
	private OfferQuotationDao offerQuotationDao;
	@Autowired
	private OfferNegotiateDao offerNegotiateDao;
	@Autowired
	private AttributeGroupDao attributeGroupDao;
	@Autowired
	private AttributeDao attributeDao;
	@Autowired
	private GlobalValueDao globalValueDao;
	@Autowired
	private LocationMemoryEngine locationMemoryEngine;
	@Autowired
	private BOQDao boqDao;

	private UUID convertToUUID(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return UUID.fromString(value);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Invalid UUID format: " + value);
		}
	}

	@Override
	public List<ProjectInfoDto> getAllProjects() {
		User user = userService.findLoggedInUser();
		List<Projects> projects;
		if ("ADMIN".equalsIgnoreCase(user.getRole())) {
			projects = projectDao.findByArchiveFalse();
		} else {
			projects = projectDao.findByCompanyIdAndArchiveFalse(user.getCompanyId());
		}
		return projects.stream().map(project -> {
			ProjectInfoDto dto = new ProjectInfoDto();
			dto.setProjectId(project.getId().toString());
			dto.setProjectName(project.getProjectName());
			dto.setProjectCode(project.getProjectCode());
			dto.setStartDate(project.getStartDate());
			dto.setEndDate(project.getEndDate());
			dto.setCreatedAt(project.getCreatedAt());
			dto.setArchieve(project.isArchive());
			BigDecimal total = project.getTotalEstimatedValue();
			dto.setTotalEstimatedValue(
					total != null ? total.doubleValue() : 0.0
			);
			dto.setRegionName(
					project.getRegion() != null ? project.getRegion().getRegionName() : null
			);
			dto.setSectorName(
					project.getSector() != null ? project.getSector().getSectorName() : null
			);
			dto.setStatus(
					project.getProjectStatus() != null ? project.getProjectStatus().getStatus() : null
			);
			dto.setCompanyId(project.getCompanyId());
			dto.setNeedFeasibility(project.isNeedFeasibility());
			return dto;
		}).collect(Collectors.toList());
	}
	@Override
	public Projects findByProjectId(String projectId) {
        return projectDao.findById(convertToUUID(projectId)).orElse(null);
	}
	@Override
	public List<ProjectInfoDto> getAllProjectsByCompanyId(String companyId) {
		if (companyId == null || companyId.isBlank()) {
			throw new DataNotFoundException("CompanyId is required");
		}

		List<Projects> projects = projectDao.findByCompanyIdAndArchiveFalse(companyId);

		if (projects == null || projects.isEmpty()) {
			throw new DataNotFoundException(
					messageSource.getMessage("project.not.found", null, Locale.ENGLISH));
		}

		return projects.stream().map(project -> {
			ProjectInfoDto dto = new ProjectInfoDto();
			dto.setProjectId(project.getId().toString());
			dto.setProjectName(project.getProjectName());
			dto.setProjectCode(project.getProjectCode());
			dto.setStartDate(project.getStartDate());
			dto.setEndDate(project.getEndDate());
			dto.setCreatedAt(project.getCreatedAt());
			dto.setArchieve(project.isArchive());
			dto.setRegionName(
					project.getRegion() != null ? project.getRegion().getRegionName() : null
			);
			dto.setSectorName(
					project.getSector() != null ? project.getSector().getSectorName() : null
			);
			dto.setStatus(
					project.getProjectStatus() != null ? project.getProjectStatus().getStatus() : null
			);
			dto.setCompanyId(project.getCompanyId());
			return dto;
		}).collect(Collectors.toList());
	}

	@Override
	public Projects saveProject(ProjectInputDTO dto, HttpServletRequest request) {
		if (dto.getProjectName() == null || dto.getProjectName().isBlank() ||
				dto.getShortName() == null || dto.getShortName().isBlank()) {
			throw new DataNotFoundException(
					messageSource.getMessage("project.name.not.found", null, Locale.ENGLISH));
		}

		if (dto.getCompanyId() == null || dto.getCompanyId().isBlank()) {
			throw new DataNotFoundException("Company is required");
		}

		Projects project = new Projects();
		project.setProjectName(dto.getProjectName().trim());
		project.setShortName(dto.getShortName().trim());
		project.setProjectCode(generateProjectCode());
		project.setNeedFeasibility(dto.isNeedFeasibility());
		project.setCompanyId(dto.getCompanyId());
		if (dto.getStartDate() != null && !dto.getStartDate().isBlank()) {
			project.setStartDate(Date.valueOf(dto.getStartDate()));
		}

		if (dto.getEndDate() != null && !dto.getEndDate().isBlank()) {
			project.setEndDate(Date.valueOf(dto.getEndDate()));
		}

		if (dto.getRegionId() != null && !dto.getRegionId().isBlank()) {
			project.setRegion(masterService.findRegionById(dto.getRegionId()));
		}

		if (dto.getSectorId() != null && !dto.getSectorId().isBlank()) {
			project.setSector(masterService.findSectorById(dto.getSectorId()));
		}

		if (dto.getUomId() != null && !dto.getUomId().isBlank()) {
			project.setUom(masterService.findUomById(dto.getUomId()));
		}

		if (dto.getScopeOfPackageIds() != null && !dto.getScopeOfPackageIds().isEmpty()) {
			project.setScopeOfPackage(
					dto.getScopeOfPackageIds().stream()
							.filter(id -> id != null && !id.isBlank())
							.map(masterService::findScopeOfPackageById)
							.filter(Objects::nonNull)
							.collect(Collectors.toSet())
			);
		}
		project.setCompanyId(dto.getCompanyId());
		User user = userService.findLoggedInUser();
		project.setCreatedBy(user.getId().toString());
		project.setCreatedAt(Date.valueOf(LocalDate.now()));
		project.setUpdateAt(Date.valueOf(LocalDate.now()));
		ProjectStatus status = masterService.findStatusByProcessUrl(request.getRequestURI());
		if (status != null) {
			project.setProjectStatus(status);
		}

		if (dto.getAddresses() != null && !dto.getAddresses().isEmpty()) {
			List<ProjectAddress> addresses = new ArrayList<>();

			for (ProjectAddressDto a : dto.getAddresses()) {
				ProjectAddress pa = new ProjectAddress();

				pa.setAddress(a.getAddress());
				pa.setEmail(a.getEmail());
				pa.setPhone(a.getPhone() != null && !a.getPhone().isBlank() ? Long.parseLong(a.getPhone()) : 0L);

				if (a.getCountry() != null && !a.getCountry().isBlank()) {
					Country country = locationMemoryEngine.findAllCountries().stream()
							.filter(c -> c.getId().equals(a.getCountry()))
							.findFirst()
							.orElse(null);

					if (country != null) {
						pa.setCountry(country.getCountry());
					}
				}

				if ((a.getCountry() != null) && (a.getState()!= null && !a.getState().isBlank())) {
					States state = locationMemoryEngine
							.findStatesByCountryId(a.getCountry())
							.stream()
							.filter(s -> s.getId().equals(a.getState()))
							.findFirst()
							.orElse(null);

					if (state != null) {
						pa.setState(state.getState());
					}
				}

				if ((a.getState() != null) && (a.getCity()!=null)) {
					Cities city = locationMemoryEngine
							.findCitiesByStateId(a.getState())
							.stream()
							.filter(c -> c.getId().equals(a.getCity()))
							.findFirst()
							.orElse(null);

					if (city != null) {
						pa.setCity(city.getCity());
					}
				}

				pa.setProject(project);
				addresses.add(pa);
			}

			project.setProjectAddresses(addresses);
		}

		if (dto.getTechFields() != null && !dto.getTechFields().isEmpty()) {
			List<ProjectTechFields> techFields = dto.getTechFields().stream().map(tf -> {
				SectorFields masterField = masterService.findSectorFieldById(tf.getTechFieldId());
				if (masterField == null) {
					throw new DataNotFoundException("Invalid tech field id: " + tf.getTechFieldId());
				}
				ProjectTechFields entity = new ProjectTechFields();
				entity.setTechField(masterField);
				entity.setValue(tf.getValue());
				entity.setProject(project);
				return entity;
			}).collect(Collectors.toList());

			project.setTechFields(techFields);
		}

		return projectDao.save(project);
	}
	private String generateProjectCode() {
		final String chars = "0123456789";
		Random random = new Random();
		StringBuilder code = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			int index = random.nextInt(chars.length());
			code.append(chars.charAt(index));
		}
		return "PRJ - " + code;
	}
	@Override
	public String saveProjectFiles(String projectId, List<MultipartFile> files) {
		Projects project = projectDao.findById(convertToUUID(projectId)).orElse(null);
        assert project != null;
		String sanitizedProjectName = project.getProjectName()
				.trim()
				.replaceAll("\\s+", "_")
				.replaceAll("[^a-zA-Z0-9-_.]", "");

		Path directory = Paths.get(uploadDir, sanitizedProjectName)
				.toAbsolutePath()
				.normalize();
		try {
			Files.createDirectories(directory);
			for (MultipartFile file : files) {
				Path targetLocation = directory.resolve(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
				Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
				ProjectDocumentDetails projectDocuments = new ProjectDocumentDetails();
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
	public void editProject(ProjectInputDTO dto, String projectId) {
		Projects project = projectDao.findById(convertToUUID(projectId))
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("project.not.found", null, Locale.ENGLISH)));
		project.setNeedFeasibility(dto.isNeedFeasibility());
		if (dto.getProjectName() != null && !dto.getProjectName().isBlank()) {
			project.setProjectName(dto.getProjectName().trim());
		}

		if (dto.getShortName() != null && !dto.getShortName().isBlank()) {
			project.setShortName(dto.getShortName().trim());
		}

		if (dto.getStartDate() != null && !dto.getStartDate().isBlank()) {
			project.setStartDate(Date.valueOf(dto.getStartDate()));
		}

		if (dto.getEndDate() != null && !dto.getEndDate().isBlank()) {
			project.setEndDate(Date.valueOf(dto.getEndDate()));
		}

		if (dto.getRegionId() != null && !dto.getRegionId().isBlank()) {
			project.setRegion(masterService.findRegionById(dto.getRegionId()));
		}

		if (dto.getSectorId() != null && !dto.getSectorId().isBlank()) {
			project.setSector(masterService.findSectorById(dto.getSectorId()));
		}

		if (dto.getUomId() != null && !dto.getUomId().isBlank()) {
			project.setUom(masterService.findUomById(dto.getUomId()));
		}

		if (dto.getScopeOfPackageIds() != null && !dto.getScopeOfPackageIds().isEmpty()) {
			project.setScopeOfPackage(
					dto.getScopeOfPackageIds().stream()
							.filter(id -> id != null && !id.isBlank())
							.map(masterService::findScopeOfPackageById)
							.filter(Objects::nonNull)
							.collect(Collectors.toSet())
			);
		}

		if (dto.getCompanyId() != null && !dto.getCompanyId().isBlank()) {
			project.setCompanyId(dto.getCompanyId());
		}

		if (dto.getAddresses() != null) {
			project.getProjectAddresses().clear();

			List<ProjectAddress> addresses = new ArrayList<>();

			for (ProjectAddressDto a : dto.getAddresses()) {

				ProjectAddress pa = new ProjectAddress();

				pa.setAddress(a.getAddress());
				pa.setEmail(a.getEmail());
				pa.setPhone(a.getPhone() != null && !a.getPhone().isBlank() ? Long.parseLong(a.getPhone()) : 0L);

				if (a.getCountry() != null && !a.getCountry().isBlank()) {
					Country country = locationMemoryEngine.findAllCountries().stream()
							.filter(c -> c.getId().equals(a.getCountry()))
							.findFirst()
							.orElse(null);

					if (country != null) {
						pa.setCountry(country.getCountry());
					}
				}

				if (a.getCountry() != null && a.getState() != null && !a.getState().isBlank()) {
					States state = locationMemoryEngine
							.findStatesByCountryId(a.getCountry())
							.stream()
							.filter(s -> s.getId().equals(a.getState()))
							.findFirst()
							.orElse(null);

					if (state != null) {
						pa.setState(state.getState());
					}
				}

				if (a.getState() != null && a.getCity() != null && !a.getCity().isBlank()) {
					Cities city = locationMemoryEngine
							.findCitiesByStateId(a.getState())
							.stream()
							.filter(c -> c.getId().equals(a.getCity()))
							.findFirst()
							.orElse(null);

					if (city != null) {
						pa.setCity(city.getCity());
					}
				}

				pa.setProject(project);
				addresses.add(pa);
			}

			project.getProjectAddresses().addAll(addresses);
		}

		if (dto.getTechFields() != null) {
			project.getTechFields().clear();

			List<ProjectTechFields> techFields = dto.getTechFields().stream().map(tf -> {
				SectorFields masterField = masterService.findSectorFieldById(tf.getTechFieldId());
				if (masterField == null) {
					throw new DataNotFoundException("Invalid tech field id: " + tf.getTechFieldId());
				}
				ProjectTechFields entity = new ProjectTechFields();
				entity.setTechField(masterField);
				entity.setValue(tf.getValue());
				entity.setProject(project);
				return entity;
			}).toList();

			project.getTechFields().addAll(techFields);
		}
		project.setUpdateAt(Date.valueOf(LocalDate.now()));
		projectDao.save(project);
	}
	@Override
	public String deleteProject(String projectId) {
		Projects project = projectDao.findById(convertToUUID(projectId)).orElse(null);
		if (project != null) {
			project.setArchive(true);
			projectDao.save(project);
		}
		return messageSource.getMessage("project.delete.success", null, Locale.ENGLISH);
	}
	@Override
	public String saveTechFeasibility(TechnicalFeasibility techFeasibility) {
		TechnicalFeasibility saved = techFeaDao.save(techFeasibility);
		return saved.getId().toString();
	}
	@Override
	public String saveFinancialFeasibility(FinancialFeasibility financialFeasibility) {
		FinancialFeasibility saved = finFeasibilityDao.save(financialFeasibility);
		return saved.getId().toString();
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
						techFeaDao.findById(convertToUUID(techFeasibilityId)).orElseThrow(() -> new DataNotFoundException(
								messageSource.getMessage("data.not.found", null, Locale.ENGLISH))));
				lastSavedId = approvalsDao.save(approvalDocuments).getId().toString();
			}
			return lastSavedId;
		}
		return null;
	}
	private String saveApprovalDocFile(MultipartFile file, String projectId) {
		Projects project = projectDao.findById(convertToUUID(projectId))
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("project.not.found", null, Locale.ENGLISH)));
		String projectName = project.getProjectName();
		Path directory = Paths
				.get(uploadDir, projectName.trim()
						.replaceAll("\\s+", "_")
						.replaceAll("[^a-zA-Z0-9-_.]", ""))
				.toAbsolutePath().normalize();
		Path targetDir = directory.resolve("approvals");
		try {
			Files.createDirectories(targetDir);
			Path targetLocation = targetDir.resolve(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			return targetLocation.toString();
		} catch (IOException e) {
			return e.getMessage();
		}
	}
	@Override
	public String saveFeasibilityStudy(String techFeasibilityId, String finFeasibilityId, String projectId) {
		FeasibilityStudy feaStudy = new FeasibilityStudy();
		Projects project = projectDao.findById(convertToUUID(projectId)).orElse(null);
		TechnicalFeasibility techFeasibility = techFeaDao.findById(convertToUUID(techFeasibilityId)).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		FinancialFeasibility finFeasibility = finFeasibilityDao.findById(convertToUUID(finFeasibilityId)).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		if (project != null ) {
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
		FeasibilityStudy feasibilityStudy = feaStudyDao.findByProjectId(UUID.fromString(projectId)).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		feasibilityStudy.setFeasibilityApproved(status);
		feasibilityStudy.setComments(comments);
		feasibilityStudy.setReviewedBy(userService.findLoggedInUser().getId().toString());
		Projects project = feasibilityStudy.getProject();
		String requestUrl = request.getRequestURI();
		ProjectStatus projectStatus = masterService.findStatusByProcessUrl(requestUrl.substring(0, requestUrl.lastIndexOf("/")));
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
	public String updateProjectStatusManual(String projectId, String statusId) {
		Projects project = projectDao.findById(convertToUUID(projectId)).orElseThrow(() -> new ProjectNotFoundException(
				messageSource.getMessage("project.not.found", null, Locale.ENGLISH)));
		ProjectStatus status = masterService.findStatusById(statusId);
		if (status == null) {
			throw new DataNotFoundException("Project Status not found");
		}
		project.setProjectStatus(status);
		projectDao.save(project);
		return "Project status updated successfully";
	}
	@Override
	public FeasibilityStudy findFeasibilityStudy(String projectId) {
		return feaStudyDao.findByProjectId(UUID.fromString(projectId)).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	}
	@Override
	public List<ApprovalDocuments> getApprovalDocData(String techId) {
        return approvalsDao.findAllByTechnicalFeasibilityId(UUID.fromString(techId));
	}
	@Override
	public void saveProjectDrawings(String projectId, List<MultipartFile> drawings) {
		Projects project = projectDao.findById(convertToUUID(projectId))
				.orElseThrow(() -> new  DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		Path directory = Paths
				.get(uploadDir,
						project.getProjectName().trim()
								.replaceAll("\\s+", "_")
								.replaceAll("[^a-zA-Z0-9-_.]", ""))
				.toAbsolutePath().normalize();
		Path targetDir = directory.resolve("drawings");
		try {
			Files.createDirectories(targetDir);
			for (MultipartFile file : drawings) {
				Path targetLocation = targetDir.resolve(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
				Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
				ProjectDocumentDetails projectDocuments = new ProjectDocumentDetails();
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
		ApprovalDocuments doc = approvalsDao.findById(convertToUUID(docId)).orElseThrow(() -> new DataNotFoundException(
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
		Set<String> projectIds = feaStudyDao.findAll().stream()
				.filter(FeasibilityStudy::isFeasibilityApproved)
				.map(project -> project.getProject().getId().toString())
				.collect(Collectors.toSet());
		return getAllProjects().stream()
				.filter(project -> projectIds.contains(project.getProjectId())
						|| !project.isNeedFeasibility())
				.collect(Collectors.toList());
	}
	@Override
	public String addActivityGroup(ActivityGroupDto activityGroupDto) {
		activityGroupsDao
				.findByActivityCodeIgnoreCaseAndActiveTrue(activityGroupDto.getActivityCode())
				.ifPresent(a -> {
					throw new DuplicateEntryException(
							messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
					);
				});
		CostCodeType costCodeType = costCodeTypeDao.findById(convertToUUID(activityGroupDto.getCostCodeTypeId()))
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		ActivityGroup activityGroup = new ActivityGroup();
		activityGroup.setActivityCode(activityGroupDto.getActivityCode());
		activityGroup.setActivityName(activityGroupDto.getActivityName());
		activityGroup.setCostCodeType(costCodeType);
		activityGroup.setActive(activityGroupDto.isActive());
		activityGroupsDao.save(activityGroup);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}
	@Override
	public void editActivityGroup(ActivityGroupDto activityGroupDto) {
		ActivityGroup existingActivityGroup = activityGroupsDao.findById(convertToUUID(activityGroupDto.getId()))
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (activityGroupDto.isActive() &&
				(
						!existingActivityGroup.getActivityCode()
								.equalsIgnoreCase(activityGroupDto.getActivityCode())
								||
								!existingActivityGroup.getActivityName()
										.equalsIgnoreCase(activityGroupDto.getActivityName())
				)
		) {
			activityGroupsDao
					.findByActivityCodeIgnoreCaseAndActiveTrue(activityGroupDto.getActivityCode())
					.ifPresent(a -> {
						throw new DuplicateEntryException(
								messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
						);
					});
		}
		CostCodeType costCodeType = costCodeTypeDao.findById(convertToUUID(activityGroupDto.getCostCodeTypeId()))
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		existingActivityGroup.setActivityCode(activityGroupDto.getActivityCode());
		existingActivityGroup.setActivityName(activityGroupDto.getActivityName());
		existingActivityGroup.setCostCodeType(costCodeType);
		existingActivityGroup.setActive(activityGroupDto.isActive());
		activityGroupsDao.save(existingActivityGroup);
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
		Projects project = projectDao.findById(convertToUUID(dtos.getFirst().getProjectId())).orElse(null);
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
		Projects project = projectDao.findById(convertToUUID(dtos.getFirst().getProjectId())).orElse(null);
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
					List<Uom> uomList = masterService.getAllUOMs();
					for (Uom uom : uomList) {
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
		Projects project = projectDao.findById(convertToUUID(dtos.getFirst().getProjectId())).orElse(null);
		List<BOQ> allBoqs = dtos.stream().flatMap(d -> d.getBoqId().stream()).filter(id -> id != null)
				.map(id -> boqDao.findById(convertToUUID(id))
						.orElseThrow(() -> new DataNotFoundException(
								messageSource.getMessage("data.not.found", null, Locale.ENGLISH))))
				.toList();
		if (allBoqs.isEmpty())
			throw new DataNotFoundException("No BOQs found for mapping");
		Set<Uom> uoms = allBoqs.stream()
				.map(BOQ::getUom)
				.collect(Collectors.toSet());
		double qty = allBoqs.stream().mapToDouble(BOQ::getQuantity).sum();
		double rate = allBoqs.stream().mapToDouble(BOQ::getTotalRate).average().orElse(0.0);
		double amount = qty * rate;
		CostCodeDto first = dtos.getFirst();
		CostCodeActivity act = mapCommonFields(first, project);
		if (uoms.size() > 1) {
			List<Uom> uomList = masterService.getAllUOMs();
			for (Uom uom : uomList) {
				if (uom.getUomCode().equalsIgnoreCase("ls")) {
					act.setUom(uom);
					break;
				}
			}
		} else {
			act.setUom(allBoqs.getFirst().getUom());
		}
		act.setBoq(new HashSet<>(allBoqs));
		act.setQuantity(qty);
		act.setRate(rate);
		act.setAmount(amount);
		costCodeActivityDao.save(act);
		return messageSource.getMessage("cost.code.save.success", null, Locale.ENGLISH);
	}
	private Set<BOQ> findBoqs(List<String> boqIds) {
		return boqIds.stream()
				.filter(Objects::nonNull)
				.map(id -> boqDao.findById(convertToUUID(id))
						.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH))))
				.collect(Collectors.toSet());
	}
	private CostCodeActivity mapCommonFields(CostCodeDto dto, Projects project) {
		CostCodeActivity costCodeAct = new CostCodeActivity();
		costCodeAct.setProject(project);
		if (dto.getActivityGroupId() != null && !dto.getActivityGroupId().isEmpty()) {
			ActivityGroup activityGroup = activityGroupsDao.findById(convertToUUID(dto.getActivityGroupId())).orElseThrow(
					() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
			costCodeAct.setActivityGroup(activityGroup);
		}
		if (dto.getCostCodeTypeId() != null && !dto.getCostCodeTypeId().isEmpty()) {
			CostCodeType costCodeType = costCodeTypeDao.findById(convertToUUID(dto.getCostCodeTypeId())).orElseThrow(
					() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
			costCodeAct.setCostCodeType(costCodeType);
		}
		if (dto.getUomId() != null && !dto.getUomId().isEmpty()) {
			Uom uom = masterService.findUomById(dto.getUomId());
			costCodeAct.setUom(uom);
		}
		costCodeAct.setActivityCode(dto.getActivityCode());
		costCodeAct.setActivityName(dto.getActivityName());
		costCodeAct.setMappingType(dto.getMappingType());
		return costCodeAct;
	}
	@Override
	public String deleteCostCode(String costCodeId) {
		CostCodeActivity costCode = costCodeActivityDao.findById(convertToUUID(costCodeId)).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		costCodeActivityDao.delete(costCode);
		return messageSource.getMessage("cost.code.delete.success", null, Locale.ENGLISH);
	}
	@Override
	public List<ProjectInfoDto> getAllCostCodeUpdatedProject() {
		List<ProjectInfoDto> projects = getAllProjects();
		Set<String> projectIds = costCodeActivityDao.findAll().stream().map(costCode -> costCode.getProject().getId().toString())
				.collect(Collectors.toSet());
		projects = projects.stream()
				.filter(project -> projectIds.contains(project.getProjectId()))
				.collect(Collectors.toList());
		return projects;
	}
	@Override
	public List<CostCodeActivity> getCostCodeActivityByProject(String projectId) {
		return costCodeActivityDao.findByProjectId(UUID.fromString(projectId));
	}
	@Override
	public CostCodeActivity getCostCodeActivityById(String costCodeId) {
		return costCodeActivityDao.findById(convertToUUID(costCodeId)).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	}
	@Override
	public double getTotalAmountByCostCodeType(String costCodeTypeId, String projectId) {
		List<CostCodeActivity> activities = costCodeActivityDao.findByProjectIdAndCostCodeTypeId(UUID.fromString(projectId),
				UUID.fromString(costCodeTypeId));
		if (activities != null && !activities.isEmpty()) {
			return activities.stream().mapToDouble(CostCodeActivity::getAmount).sum();
		}
		return 0;
	}
	@Override
	public ActivityGroup getActivityGroupById(String activityGroupId) {
		return activityGroupsDao.findById(convertToUUID(activityGroupId)).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	}
	@Override
	public void addTender(TenderInputDto tenderInputDto, String technicalNotes,
						  List<MultipartFile> technicalTerms, String commercialNotes,
						  List<MultipartFile> commercialTerms, String drawingsNotes,
						  List<MultipartFile> drawings, String othersNotes,
						  List<MultipartFile> others, HttpServletRequest request) {

		Tender tender;
		boolean isEdit = false;

		if (tenderInputDto.getId() != null && !tenderInputDto.getId().isEmpty()) {
			Optional<Tender> existingOpt = tenderDao.findById(convertToUUID(tenderInputDto.getId()));
			if (existingOpt.isPresent()) {
				User user = userService.findLoggedInUser();
				tender = existingOpt.get();
				isEdit = true;
				trackTenderHistory(tender, tenderInputDto, user.getId().toString(), tenderInputDto.getFloatedBy());
			} else {
				tender = new Tender();
				tender.setId(UUID.fromString(tenderInputDto.getId()));
			}
		} else {
			tender = new Tender();
		}
		tender.setTenderNumber(tenderInputDto.getTenderFloatingNo());
		tender.setTenderName(tenderInputDto.getTenderName());
		tender.setBidOpeningdate(tenderInputDto.getBidOpeningDate());
		tender.setLastDate(tenderInputDto.getLastDate());
		tender.setFloatedUser(tenderInputDto.getFloatedBy());
		tender.setContactName(tenderInputDto.getContactName());
		tender.setContactNumber(tenderInputDto.getContactNumber());
		tender.setContactEmail(tenderInputDto.getContactEmail());
		tender.setSiteInvestigation(tenderInputDto.isSiteInvestigation());
		tender.setPreBidding(tenderInputDto.isPreBidMeeting());
		tender.setPreBiddingDate(tenderInputDto.getPreBidMeetingDate());
		tender.setPreBiddingTime(tenderInputDto.getPreBidMeetingTime());
		tender.setSiteInvestigationFromDate(tenderInputDto.getSiteInvestigationFromDate());
		tender.setSiteInvestigationToDate(tenderInputDto.getSiteInvestigationToDate());
		tender.setSiteInvestigationFromTime(tenderInputDto.getSiteInvestigationFromTime());
		tender.setSiteInvestigationToTime(tenderInputDto.getSiteInvestigationToTime());
		tender.setFloatedOn(tenderInputDto.getTenderFloatingDate());

		if (tenderInputDto.getSubmissionMode() != null) {
			tender.setSubmissionMode(OfferSubmissionMode.valueOf(tenderInputDto.getSubmissionMode()));
		}

		Projects project = null;
		if (tenderInputDto.getProjectId() != null) {
			project = projectDao.findById(convertToUUID(tenderInputDto.getProjectId())).orElse(null);
			tender.setProjects(project);
		}

		if (tenderInputDto.getScopeId() != null) {
			Set<ScopeOfPackage> scopeSet = new HashSet<>();
			for (String id : tenderInputDto.getScopeId()) {
				ScopeOfPackage scope = masterService.findScopeOfPackageById(id);
				if (scope != null) scopeSet.add(scope);
			}
			tender.setScopeOfPackages(scopeSet);
		}

		if (tenderInputDto.getBoqId() != null) {

			Set<UUID> boqIds = tenderInputDto.getBoqId()
					.stream()
					.map(UUID::fromString)
					.collect(Collectors.toSet());

			tender.setBoq(new HashSet<>(boqDao.findAllById(boqIds)));
		}
		List<Contractor> contractorsToEmail = new ArrayList<>();
		if (tenderInputDto.getContractorId() != null) {
			Set<Contractor> currentContractors = tender.getContractor();
			if (currentContractors == null) currentContractors = new HashSet<>();

			Set<Contractor> updatedContractors = new HashSet<>();

			for (String id : tenderInputDto.getContractorId()) {
				Contractor contractor = contractorService.findContractorById(id);
				if (contractor != null) {
					updatedContractors.add(contractor);
					if (!isEdit || !currentContractors.contains(contractor)) {
						contractorsToEmail.add(contractor);
					}
				}
			}
			tender.setContractor(updatedContractors);
		}

		if (!isEdit) {
			tender.setTenderStatus(TenderStatus.PENDING);
		}

		tender = tenderDao.save(tender);

		handleAttachments(tender, project, technicalNotes, technicalTerms, commercialNotes, commercialTerms, drawingsNotes, drawings, othersNotes, others);

		String requestUrl = request.getRequestURI();
		ProjectStatus status = masterService.findStatusByProcessUrl(requestUrl.substring(0, requestUrl.lastIndexOf("/")));
		if (status != null && project != null) {
			updateProjectStatus(project, status);
		}

		addInviteDetails(tender);

		if (!contractorsToEmail.isEmpty()) {
			emailService.sendquoteMail(contractorsToEmail, tender);
		}
	}
	private void trackTenderHistory(Tender existingTender, TenderInputDto newDetails, String userId, String userName) {
		List<TenderEditHistory> historyList = new ArrayList<>();

		compareAndLog(historyList, existingTender, "Tender Name",
				existingTender.getTenderName(), newDetails.getTenderName(), userId, userName);

		compareAndLog(historyList, existingTender, "Tender Number",
				existingTender.getTenderNumber(), newDetails.getTenderFloatingNo(), userId, userName);

		compareAndLog(historyList, existingTender, "Contact Name",
				existingTender.getContactName(), newDetails.getContactName(), userId, userName);

		compareAndLog(historyList, existingTender, "Contact Number",
				existingTender.getContactNumber(), newDetails.getContactNumber(), userId, userName);

		compareAndLog(historyList, existingTender, "Contact Email",
				existingTender.getContactEmail(), newDetails.getContactEmail(), userId, userName);

		compareAndLog(historyList, existingTender, "Bid Opening Date",
				toStringSafe(existingTender.getBidOpeningdate()), toStringSafe(newDetails.getBidOpeningDate()), userId, userName);

		compareAndLog(historyList, existingTender, "Last Date",
				toStringSafe(existingTender.getLastDate()), toStringSafe(newDetails.getLastDate()), userId, userName);

		compareAndLog(historyList, existingTender, "Pre-Bid Meeting Date",
				toStringSafe(existingTender.getPreBiddingDate()), toStringSafe(newDetails.getPreBidMeetingDate()), userId, userName);

		compareAndLog(historyList, existingTender, "Pre-Bid Meeting Time",
				toStringSafe(existingTender.getPreBiddingTime()), toStringSafe(newDetails.getPreBidMeetingTime()), userId, userName);

		compareAndLog(historyList, existingTender, "Site Inv.From Date",
				toStringSafe(existingTender.getSiteInvestigationFromDate()), toStringSafe(newDetails.getSiteInvestigationFromDate()), userId, userName);

		compareAndLog(historyList, existingTender, "Site Inv.To Date",
				toStringSafe(existingTender.getSiteInvestigationToDate()), toStringSafe(newDetails.getSiteInvestigationToDate()), userId, userName);

		compareAndLog(historyList, existingTender, "Site Inv.From Time",
				toStringSafe(existingTender.getSiteInvestigationFromTime()), toStringSafe(newDetails.getSiteInvestigationFromTime()), userId, userName);

		compareAndLog(historyList, existingTender, "Site Inv.To Time",
				toStringSafe(existingTender.getSiteInvestigationToTime()), toStringSafe(newDetails.getSiteInvestigationToTime()), userId, userName);

		String oldSubmissionMode = existingTender.getSubmissionMode() != null ? existingTender.getSubmissionMode().name() : "N/A";
		String newSubmissionMode = newDetails.getSubmissionMode();
		if (newSubmissionMode != null && !oldSubmissionMode.equals(newSubmissionMode)) {
			createHistoryEntry(historyList, existingTender, "Submission Mode", oldSubmissionMode, newSubmissionMode, userId, userName);
		}

		String oldScopes = existingTender.getScopeOfPackages() != null ?
				existingTender.getScopeOfPackages().stream().map(ScopeOfPackage::getScope).collect(Collectors.joining(", ")) : "";

		String newScopes = "";
		if (newDetails.getScopeId() != null && !newDetails.getScopeId().isEmpty()) {
			List<ScopeOfPackage> newScopeEntities = newDetails.getScopeId().stream()
					.map(id-> masterService.findScopeOfPackageById(id))
					.toList();
			newScopes = newScopeEntities.stream().map(ScopeOfPackage::getScope).collect(Collectors.joining(", "));
		}
		compareAndLog(historyList, existingTender, "Scope of Packages", oldScopes, newScopes, userId, userName);

		String oldBoqs = existingTender.getBoq() != null ?
				existingTender.getBoq().stream().map(BOQ::getBoqCode).collect(Collectors.joining(", ")) : "";

		String newBoqs = "";
		if (newDetails.getBoqId() != null && !newDetails.getBoqId().isEmpty()) {
			List<UUID> boqIds = newDetails.getBoqId()
					.stream()
					.map(UUID::fromString)
					.toList();
			List<BOQ> newBoqEntities = boqDao.findAllById(boqIds);
			newBoqs = newBoqEntities.stream().map(BOQ::getBoqCode).collect(Collectors.joining(", "));
		}
		compareAndLog(historyList, existingTender, "BOQs", oldBoqs, newBoqs, userId, userName);

		String oldContractors = existingTender.getContractor() != null ?
				existingTender.getContractor().stream().map(Contractor::getEntityName).collect(Collectors.joining(", ")) : "";

		String newContractors = "";
		if (newDetails.getContractorId() != null && !newDetails.getContractorId().isEmpty()) {
			List<Contractor> newContractorEntities = newDetails.getContractorId().stream()
					.map(id -> contractorService.findContractorById(id))
					.toList();
			newContractors = newContractorEntities.stream().map(Contractor::getEntityName).collect(Collectors.joining(", "));
		}
		compareAndLog(historyList, existingTender, "Contractors", oldContractors, newContractors, userId, userName);

		if (!historyList.isEmpty()) {
			tenderEditHistoryDao.saveAll(historyList);
		}
	}

	private void compareAndLog(List<TenderEditHistory> list, Tender tender, String fieldName, String oldVal, String newVal, String userId, String userName) {
		String safeOld = oldVal == null ? "" : oldVal.trim();
		String safeNew = newVal == null ? "" : newVal.trim();

		if (!safeOld.equals(safeNew)) {
			createHistoryEntry(list, tender, fieldName, safeOld, safeNew, userId, userName);
		}
	}

	private void createHistoryEntry(List<TenderEditHistory> list, Tender tender, String field, String oldVal, String newVal, String userId, String userName) {
		TenderEditHistory history = new TenderEditHistory();
		history.setTender(tender);
		history.setField(field);
		history.setOldValue(oldVal);
		history.setNewValue(newVal);
		history.setUpdatedBy(userName);
		history.setUpdatedUserId(userId);
		history.setRemarks("Tender Updated");
		history.setCreatedAt(new Timestamp(System.currentTimeMillis()));
		list.add(history);
	}

	private String toStringSafe(Object date) {
		return date == null ? "" : String.valueOf(date);
	}
	private void handleAttachments(Tender tender, Projects project,
								   String technicalNotes, List<MultipartFile> technicalTerms,
								   String commercialNotes, List<MultipartFile> commercialTerms,
								   String drawingsNotes, List<MultipartFile> drawings,
								   String othersNotes, List<MultipartFile> others) {

		TenderAttachments attachments = tenderAttachmentDao.findByTenderId(tender.getId()).orElse(null);

		if (attachments == null) {
			attachments = new TenderAttachments();
			attachments.setTender(tender);
		}

		attachments.setProject(project);

		if (technicalNotes != null) attachments.setTechnicalTerms(technicalNotes);
		if (commercialNotes != null) attachments.setCommercialTerms(commercialNotes);
		if (drawingsNotes != null) attachments.setDrawings(drawingsNotes);
		if (othersNotes != null) attachments.setOthers(othersNotes);

		if (project != null) {
			if (technicalTerms != null && !technicalTerms.isEmpty())
				attachments.setTechnicalTermsUrl(saveTenderFiles(project, technicalTerms));

			if (commercialTerms != null && !commercialTerms.isEmpty())
				attachments.setCommercialTermsUrl(saveTenderFiles(project, commercialTerms));

			if (drawings != null && !drawings.isEmpty())
				attachments.setDrawingUrl(saveTenderFiles(project, drawings));

			if (others != null && !others.isEmpty())
				attachments.setOtherUrl(saveTenderFiles(project, others));
		}
		tenderAttachmentDao.save(attachments);
	}

	private void addInviteDetails(Tender tender) {
		if (tender.getContractor() == null) {
			return;
		}
		for (Contractor contractor : tender.getContractor()) {
			boolean inviteExists = tenderInviteDao.existsByTenderIdAndContractorId(tender.getId(), contractor.getId());

			if (!inviteExists) {
				TenderInvites invite = new TenderInvites();
				invite.setTender(tender);
				invite.setContractor(contractor);
				invite.setSubmitted(false);
				invite.setTempToken(UUID.randomUUID().toString());
				invite.setInvitedOn(new Date(System.currentTimeMillis()));
				long expiryTime = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000);
				invite.setTokenExpiry(new java.sql.Timestamp(expiryTime));
				tenderInviteDao.save(invite);
			}
		}
	}

	private List<String> saveTenderFiles(Projects project, List<MultipartFile> files) {
		List<String> fileUrls = new ArrayList<>();
		if (files == null || files.isEmpty()) {
			return fileUrls;
		}
		Path directory = Paths
				.get(uploadDir,
						project.getProjectName().trim().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9-_\\.]", ""))
				.toAbsolutePath().normalize();
		try {
			if (!Files.exists(directory)) {
				Files.createDirectories(directory);
			}
			for (MultipartFile file : files) {
				if (file.isEmpty()) continue;
				String cleanFileName = StringUtils.cleanPath(file.getOriginalFilename());
				Path targetLocation = directory.resolve(cleanFileName);
				Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
				fileUrls.add(targetLocation.toString());
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not store file. Error: " + e.getMessage());
		}
		return fileUrls;
	}
	@Override
	public List<ProjectInfoDto> getTenderFloatedProject() {
		List<ProjectInfoDto> projects = getAllProjects();
		Set<String> projectIds = tenderDao.findAll().stream()
				.map(tender -> tender.getProjects().getId().toString())
				.collect(Collectors.toSet());
		projects = projects.stream()
				.filter(project -> projectIds.contains(project.getProjectId()))
				.collect(Collectors.toList());
		return projects;
	}

	@Override
	public List<Tender> getAllTenderByProject(String projectId) {
		List<Tender> tender = tenderDao.findByProjectsId(UUID.fromString(projectId));
		return tender;
	}

    @Override
    public TenderAttachments getAllAttachementsByTenderId(String tenderId) {
        return tenderAttachmentDao.findByTenderId(UUID.fromString(tenderId))
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("tender.attachment.not.found", null, Locale.ENGLISH)));
    }

	@Override
	public TenderRespDto getTenderByTenderId(String tenderId) {
		Tender tender = tenderDao.findById(convertToUUID(tenderId))
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("tender.not.found", null, Locale.ENGLISH)));
		TenderRespDto tenderResponse = new TenderRespDto();
		tenderResponse.setId(tender.getId().toString());
		tenderResponse.setTenderName(tender.getTenderName());
		tenderResponse.setTenderNumber(tender.getTenderNumber());
		tenderResponse.setContactName(tender.getContactName());
		tenderResponse.setContactEmail(tender.getContactEmail());
		tenderResponse.setContactNumber(tender.getContactNumber());
		tenderResponse.setBidOpeningdate(tender.getBidOpeningdate());
		tenderResponse.setLastDate(tender.getLastDate());
		tenderResponse.setOfferSubmissionMode(tender.getSubmissionMode().getLabel());
		tenderResponse.setFloatedUser(tender.getFloatedUser());
		tenderResponse.setFloatedOn(tender.getFloatedOn());
		tenderResponse.setSiteInvestigation(tender.isSiteInvestigation());
		tenderResponse.setPreBidding(tender.isPreBidding());
		tenderResponse.setPreBiddingDate(tender.getPreBiddingDate());
		tenderResponse.setPreBiddingTime(tender.getPreBiddingTime());
		tenderResponse.setSiteInvestigationFromDate(tender.getSiteInvestigationFromDate());
		tenderResponse.setSiteInvestigationToDate(tender.getSiteInvestigationToDate());
		tenderResponse.setSiteInvestigationFromTime(tender.getSiteInvestigationFromTime());
		tenderResponse.setSiteInvestigationToTime(tender.getSiteInvestigationToTime());
		tenderResponse.setSiteInvestigationComments(tender.getSiteInvestigationComments());
		tenderResponse.setPreBiddingComments(tender.getPreBiddingComments());
		if (tender.getScopeOfPackages() != null) {
			tenderResponse.setScopeOfPackages(tender.getScopeOfPackages());
		}
		if (tender.getBoq() != null) {
			Set<BOQDto> boqDtos = tender.getBoq().stream().map(b -> {
				BOQDto dto = new BOQDto();
				dto.setBoqId(b.getId().toString());
				dto.setBoqCode(b.getBoqCode());
				dto.setBoqName(b.getBoqName());
				dto.setQuantity(b.getQuantity());
				dto.setUom(b.getUom());
				return dto;
			}).collect(Collectors.toSet());
			tenderResponse.setBoq(boqDtos);
		}
		return tenderResponse;
	}
	@Override
	public TenderOffers saveOffer(OfferDTO offerDTO) {
		TenderOffers offers;
		int currentVersion = 1;

		if (offerDTO.getId() != null && !offerDTO.getId().isEmpty()) {
			offers = tenderOffersDao.findById(convertToUUID(offerDTO.getId()))
					.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("offer.not.found", null, Locale.ENGLISH)));
			List<OfferQuotation> offerQuotations = offerQuotationDao.findByTenderOffersId(UUID.fromString(offerDTO.getId()));
			int maxExistingVersion = offerQuotations.stream()
					.mapToInt(OfferQuotation::getOfferVersion)
					.max()
					.orElse(0);
			currentVersion = maxExistingVersion + 1;
			offers.setSubmittedOn(new Date(System.currentTimeMillis()));
			offers.setOfferStatus(OfferStatus.SUBMITTED);
		} else {
			offers = new TenderOffers();
			Tender tender = tenderDao.findById(convertToUUID(offerDTO.getTenderId()))
					.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("tender.not.found", null, Locale.ENGLISH)));
			offers.setTender(tender);
			if (tender.getProjects() != null) {
				offers.setProjects(tender.getProjects());
			} else {
				throw new DataNotFoundException(messageSource.getMessage("project.not.found", null, Locale.ENGLISH));
			}
			Contractor contractor = contractorService.findContractorById(offerDTO.getContractorId());
			offers.setContractor(contractor);
			offers.setSubmittedOn(new Date(System.currentTimeMillis()));
			offers.setOfferStatus(OfferStatus.SUBMITTED);

			List<TenderOffers> existingOffers = tenderOffersDao.findByTenderIdAndContractorId(tender.getId(), contractor.getId());
			int maxVersion = 0;
			if (existingOffers != null && !existingOffers.isEmpty()) {
				for (TenderOffers exOffer : existingOffers) {
					if (exOffer.getOfferQuotations() != null) {
						for (OfferQuotation q : exOffer.getOfferQuotations()) {
							if (q.getOfferVersion() > maxVersion) {
								maxVersion = q.getOfferVersion();
							}
						}
					}
				}
				if (maxVersion < existingOffers.size()) {
					maxVersion = existingOffers.size();
				}
			}
			currentVersion = maxVersion + 1;
		}

		List<OfferQuotation> newQuotations = new ArrayList<>();
		if (offerDTO.getBoqRate() != null) {
			for (Map<String, Double> rateMap : offerDTO.getBoqRate()) {
				for (Map.Entry<String, Double> entry : rateMap.entrySet()) {
					BOQ boq = boqDao.findById(convertToUUID(entry.getKey()))
							.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("boq.not.found", null, Locale.ENGLISH)));

					OfferQuotation quotation = new OfferQuotation();
					quotation.setTenderOffers(offers);
					quotation.setBoq(boq);
					quotation.setRate(entry.getValue());
					quotation.setQuantity(boq.getQuantity());
					quotation.setAmount(entry.getValue() * boq.getQuantity());
					quotation.setOfferVersion(currentVersion);
					quotation.setPaymentTerms(offerDTO.getPaymentTerms());
					newQuotations.add(quotation);
				}
			}
		}

		if (offers.getOfferQuotations() == null) {
			offers.setOfferQuotations(newQuotations);
		} else {
			offers.getOfferQuotations().addAll(newQuotations);
		}

		return tenderOffersDao.save(offers);
	}
	@Override
	public Tender getTenderDetailsByTenderId(String tenderId) {
		return tenderDao.findById(convertToUUID(tenderId)).orElseThrow(()-> new DataNotFoundException(
				messageSource.getMessage("tender.not.found",null,Locale.ENGLISH)
		));
	}
	@Override
	public List<TenderEditHistory> getEditHistoriesByTenderId(String tenderId) {
		return tenderEditHistoryDao.findByTenderId(UUID.fromString(tenderId));
	}

	@Override
	public List<TenderOffers> getAllOfferByTenderId(String tenderId) {
		List<TenderOffers> offers = tenderOffersDao.findByTenderId(UUID.fromString(tenderId));
		if (offers != null && !offers.isEmpty()) {
			Map<UUID, List<TenderOffers>> byContractor = offers.stream()
					.filter(o -> o.getContractor() != null && o.getContractor().getId() != null)
					.collect(Collectors.groupingBy(o -> o.getContractor().getId()));

			for (List<TenderOffers> contractorOffers : byContractor.values()) {
				if (contractorOffers.size() > 1) {
					boolean hasDuplicates = contractorOffers.stream()
							.filter(o -> o.getOfferQuotations() != null && !o.getOfferQuotations().isEmpty())
							.map(o -> o.getOfferQuotations().get(0).getOfferVersion())
							.distinct()
							.count() < contractorOffers.size();

					if (hasDuplicates) {
						contractorOffers.sort((a, b) -> {
							if (a.getSubmittedOn() != null && b.getSubmittedOn() != null) {
								int cmp = a.getSubmittedOn().compareTo(b.getSubmittedOn());
								if (cmp != 0) return cmp;
							}
							return a.getId().compareTo(b.getId());
						});

						int seqVersion = 1;
						List<OfferQuotation> toUpdate = new ArrayList<>();
						for (TenderOffers o : contractorOffers) {
							if (o.getOfferQuotations() != null) {
								for (OfferQuotation q : o.getOfferQuotations()) {
									if (q.getOfferVersion() != seqVersion) {
										q.setOfferVersion(seqVersion);
										toUpdate.add(q);
									}
								}
							}
							seqVersion++;
						}
						if (!toUpdate.isEmpty()) {
							offerQuotationDao.saveAll(toUpdate);
						}
					}
				}
			}
		}
		return offers;
	}

	@Override
	public String negotiateTender(NegotiateDTO negotiateDTO) {
		OfferNegotiation negotiation = new OfferNegotiation();
		TenderOffers offer = tenderOffersDao.findById(convertToUUID(negotiateDTO.getOfferId()))
				.orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("offer.not.found",null,Locale.ENGLISH)));
		negotiation.setTenderOffer(offer);
		negotiation.setComments(negotiateDTO.getComments());
		negotiation.setOfferVersion(negotiateDTO.getOfferVersion());
		TenderInvites invite = tenderInviteDao.findByContractor_IdAndTender_Id(UUID.fromString(negotiateDTO.getContractorId()),UUID.fromString(negotiateDTO.getTenderId()))
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("tender.invite.not.found",null,Locale.ENGLISH)));
		invite.setSubmitted(false);
		invite.setTokenExpiry(new Timestamp(System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000)));
		offer.setOfferStatus(OfferStatus.NEGOTIATION);
		tenderInviteDao.save(invite);
		tenderOffersDao.save(offer);
		offerNegotiateDao.save(negotiation);
		String mailId = contractorService.findContractorById(negotiateDTO.getContractorId())
				.getContractorContacts()
				.getFirst()
				.getEmail();
		emailService.sentNegotiateMail(negotiation,mailId);
		return "Updated Successfully";
	}

	@Override
	public TenderRespDto findOfferByOfferVersion(String tenderId, String contractorId, int offerVersion) {
		List<TenderOffers> offersList = tenderOffersDao.findByTenderIdAndContractorId(UUID.fromString(tenderId), UUID.fromString(contractorId));
		if (offersList.isEmpty()) {
			throw new DataNotFoundException(messageSource.getMessage("tender.offer.not.found", null, Locale.ENGLISH));
		}
		List<OfferQuotation> quote = offersList.stream()
				.filter(o -> o.getOfferQuotations() != null)
				.flatMap(o -> o.getOfferQuotations().stream())
				.filter(offer -> offer.getOfferVersion() == offerVersion)
				.toList();
		Tender tender = offersList.get(0).getTender();
		TenderRespDto tenderResponse = new TenderRespDto();
		tenderResponse.setId(tender.getId().toString());
		tenderResponse.setTenderName(tender.getTenderName());
		tenderResponse.setTenderNumber(tender.getTenderNumber());
		tenderResponse.setContactName(tender.getContactName());
		tenderResponse.setContactEmail(tender.getContactEmail());
		tenderResponse.setContactNumber(tender.getContactNumber());
		tenderResponse.setBidOpeningdate(tender.getBidOpeningdate());
		tenderResponse.setLastDate(tender.getLastDate());
		tenderResponse.setOfferSubmissionMode(tender.getSubmissionMode().getLabel());
		tenderResponse.setFloatedUser(tender.getFloatedUser());
		tenderResponse.setFloatedOn(tender.getFloatedOn());
		tenderResponse.setSiteInvestigation(tender.isSiteInvestigation());
		tenderResponse.setPreBidding(tender.isPreBidding());
		tenderResponse.setPreBiddingDate(tender.getPreBiddingDate());
		tenderResponse.setPreBiddingTime(tender.getPreBiddingTime());
		tenderResponse.setSiteInvestigationFromDate(tender.getSiteInvestigationFromDate());
		tenderResponse.setSiteInvestigationToDate(tender.getSiteInvestigationToDate());
		tenderResponse.setSiteInvestigationFromTime(tender.getSiteInvestigationFromTime());
		tenderResponse.setSiteInvestigationToTime(tender.getSiteInvestigationToTime());
		tenderResponse.setSiteInvestigationComments(tender.getSiteInvestigationComments());
		tenderResponse.setPreBiddingComments(tender.getPreBiddingComments());
		if (tender.getScopeOfPackages() != null) {
			tenderResponse.setScopeOfPackages(tender.getScopeOfPackages());
		}
		if (quote != null && !quote.isEmpty()) {
			Set<BOQDto> boqDtos = quote.stream()
					.map(offer -> {
						BOQDto dto= new BOQDto();
						dto.setBoqId(offer.getBoq().getId().toString());
						dto.setBoqCode(offer.getBoq().getBoqCode());
						dto.setBoqName(offer.getBoq().getBoqName());
						dto.setQuantity(offer.getQuantity());
						dto.setRate(offer.getRate());
						dto.setAmount(offer.getAmount());
						dto.setUom(offer.getBoq().getUom());
						return dto;
			}).collect(Collectors.toSet());
			tenderResponse.setBoq(boqDtos);
		}
		return tenderResponse;
	}

	@Override
	public int findOfferNegotiation(String negotiationId) {
		OfferNegotiation offerNegotiation = offerNegotiateDao.findById(convertToUUID(negotiationId))
				.orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("tender.offer.not.found",null,Locale.ENGLISH)));
		return offerNegotiation.getOfferVersion();
	}
	@Override
	public String awardTender(String tenderId, String tenderOfferId, String offerQuotationId) {
		Tender tender = tenderDao.findById(convertToUUID(tenderId))
				.orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("tender.not.found",null,Locale.ENGLISH)));
		TenderOffers tenderOffers = tenderOffersDao.findById(convertToUUID(tenderOfferId))
				.orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("tender.offer.not.found",null,Locale.ENGLISH)));
		OfferQuotation quotation = offerQuotationDao.findById(convertToUUID(offerQuotationId))
				.orElse(null);
		tender.setOfferQuotation(quotation);
		tender.setTenderOffers(tenderOffers);
		tender.setTenderStatus(TenderStatus.AWARD);
		tenderOffers.setOfferStatus(OfferStatus.AWARDED);
		tenderDao.save(tender);
		return "Tender Awarded successfully";
	}

}