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
	private TenderEstimationDao tenderEstDao;
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
	private InternalBoqDao internalBoqDao;

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
		if (fileName!=null && (fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))) {
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
		
	    List<ExcelExtractBOQ> allRows ;
	    String fileName = boqFile.getOriginalFilename();
		if (fileName != null && (fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))) {
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
	    return new PaginatedResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages
        );
	}
	@Override
	public String saveBoqFile(MultipartFile boqFile, String projectId) {
		Projects project = projectDao.findById(convertToUUID(projectId))
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("project.not.found", null, Locale.ENGLISH)));
		String projectName = project.getProjectName();
		Path directory = Paths
				.get(uploadDir, projectName.trim()
						.replaceAll("\\s+", "_")
						.replaceAll("[^a-zA-Z0-9-_.]", ""))
				.toAbsolutePath().normalize().resolve("BOQ");
		try {
			if (boqFile == null || boqFile.isEmpty()) {
				throw new FileNotSaveException("Uploaded file is empty");
			}
			String fileName = StringUtils.cleanPath(Objects.requireNonNull(boqFile.getOriginalFilename()));
			if (fileName.isBlank() || fileName.contains("..")) {
				throw new FileNotSaveException("Invalid file name: " + fileName);
			}
			Files.createDirectories(directory);
			Path targetLocation = directory.resolve(fileName);
			Files.copy(boqFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			String filePath = targetLocation.toString();
			List<BOQFile> boqFiles = boqFileDao.findByProjectId(UUID.fromString(projectId));
			if (boqFiles.stream().noneMatch(b -> b.getFilename().equals(fileName))) {
				BOQFile boqFileEntity = new BOQFile();
				boqFileEntity.setFilename(fileName);
				boqFileEntity.setFileUrl(filePath);
				boqFileEntity.setProject(projectDao.findById(convertToUUID(projectId)).orElse(null));
				boqFileDao.save(boqFileEntity);
			}
			return filePath;
		} catch (IOException e) {
			throw new FileNotSaveException(messageSource.getMessage("file.upload.failure", null, Locale.ENGLISH), e);
		}
	}
	@Override
	public String mapBOQData(String projectId,
							 String sheetName,
							 Map<String, String> columnMapping,
							 Map<Long, Long> parentChildMapping,
							 Map<Long, Boolean> lastLevelMapping,
							 Map<Long, Long> levelMapping,
							 MultipartFile boqFile) throws IOException {
		Projects project = projectDao.findById(convertToUUID(projectId)).orElse(null);
		saveBoqFile(boqFile, projectId);
		int startRow = 0;
		int endRow = 100000;
		List<ExcelExtractBOQ> extractedRows;
		String fileName = boqFile.getOriginalFilename();
		if (fileName != null && (fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))) {
			extractedRows = excelService.getExtractedData(boqFile, sheetName, columnMapping, startRow, endRow);
		} else {
			extractedRows = pdfService.getExtractedData(boqFile, columnMapping, startRow, endRow);
		}
		if (extractedRows == null || extractedRows.isEmpty()) {
			throw new DataNotFoundException("No BOQ data extracted from file!");
		}
		Map<String, BOQ> boqUniqueMap = new HashMap<>();
		Map<Long, BOQ> rowToBoqMap = new HashMap<>();
		for (ExcelExtractBOQ row : extractedRows) {
			if (!isValidText(row.getBoqCode()) && !isNotEmpty(row.getBoqCode())) {
				continue;
			}
			String key = (row.getBoqCode() + "_" + row.getBoqName()).toLowerCase();
			BOQ boq = boqUniqueMap.get(key);
			if (boq == null) {
				boq = boqDao
						.findByProject_IdAndBoqCodeIgnoreCaseAndBoqNameIgnoreCase(
								project.getId(),
								row.getBoqCode(),
								row.getBoqName()
						)
						.orElseGet(BOQ::new);
				if (boq.getId() == null) {
					boq.setProject(project);
				}
				boq.setBoqCode(row.getBoqCode());
				boq.setBoqName(row.getBoqName());
				if (row.getUom() != null) {
					boq.setUom(findUOM(row.getUom()));
				}
				boqUniqueMap.put(key, boq);
			}
			boq.setQuantity(row.getQuantity());
			Long level = levelMapping.getOrDefault(row.getSno(), 0L);
			boq.setLevel(level.intValue());
			if (!row.isLastLevel()) {
				boq.setLastLevel(lastLevelMapping.getOrDefault(row.getSno(), false));
			} else {
				boq.setLastLevel(row.isLastLevel());
			}
			rowToBoqMap.put(row.getSno(), boq);
		}
		parentChildMapping.forEach((childSno, parentSno) -> {
			BOQ child = rowToBoqMap.get(childSno);
			BOQ parent = rowToBoqMap.get(parentSno);
			if (child != null && parent != null && child != parent) {
				child.setParentBOQ(parent);
				parent.getChildren().add(child);
			}
		});
		for (ExcelExtractBOQ row : extractedRows) {
			Long childSno = row.getSno();
			Long parentSno = row.getParentSno();
			if (parentSno == null || parentSno == 0) continue;
			if (parentChildMapping.containsKey(childSno)) continue;
			BOQ child = rowToBoqMap.get(childSno);
			BOQ parent = rowToBoqMap.get(parentSno);
			if (child != null && parent != null && child != parent) {
				child.setParentBOQ(parent);
				parent.getChildren().add(child);
			}
		}
		boqDao.saveAll(new ArrayList<>(boqUniqueMap.values()));
		return "BOQ Mapping Successfully Stored for Project " + projectId;
	}

	protected Uom findUOM(String value) {
		String sanitizedValue = value.trim().replaceAll("[., /]", "");
		List<Uom> uoms = masterService.getAllUOMs();
		for (Uom uom : uoms) {
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

	@Override
	public String updateBOQHierarchy(String projectId, Map<String, String> parentChildUpdateMap) {
		UUID projId = convertToUUID(projectId);
		List<BOQ> boqList = boqDao.findByProjectId(projId);
		if (boqList == null || boqList.isEmpty()) {
			throw new DataNotFoundException("No BOQs found for project");
		}
		Map<UUID, BOQ> boqMap = boqList.stream()
				.collect(Collectors.toMap(BOQ::getId, boq -> boq));
		for (Map.Entry<String, String> entry : parentChildUpdateMap.entrySet()) {
			UUID childId = convertToUUID(entry.getKey());
			UUID parentId = convertToUUID(entry.getValue());
			if (childId.equals(parentId)) {
				throw new RuntimeException("BOQ cannot be parent of itself: " + childId);
			}
			BOQ child = boqMap.get(childId);
			BOQ newParent = boqMap.get(parentId);
			if (child == null || newParent == null) {
				throw new RuntimeException("Invalid BOQ mapping: " + childId + " -> " + parentId);
			}
			child.setParentBOQ(newParent);
			if (!newParent.getChildren().contains(child)) {
				newParent.getChildren().add(child);
			}
		}
		boqDao.saveAll(boqList);
		return "BOQ hierarchy updated successfully for project: " + projectId;
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
		List<BOQ> boqList = boqDao.findByProjectId(UUID.fromString(projectId));
		if (boqList.isEmpty()) {
			return null;
		}
        return boqList.stream()
                .filter(boq -> boq.getLevel() == 1)
                .collect(Collectors.toList());
	}
	@Override
	public Page<BOQ> getParentBOQs(String projectId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("boqCode").ascending());
		Page<BOQ> boqPage = boqDao.findByProjectIdAndParentBOQIsNull(convertToUUID(projectId), pageable);
		if (boqPage.isEmpty()) {
			throw new DataNotFoundException("No BOQ data found");
		}
		return boqPage;
	}
	@Override
	public List<BOQ> getChildBOQ(String projectId, String parentBoqId) {
		List<BOQ> boqList = boqDao.findByProjectIdAndParentBOQId(UUID.fromString(projectId), UUID.fromString(parentBoqId));
		if (boqList.isEmpty()) {
			return null;
		}
		return boqList;
	}
	@Override
	public Map<String, Integer> getBOQCount(String projectId) {
		List<BOQ> boqList = boqDao.findByProjectId(UUID.fromString(projectId));
		Map<String, Integer> boqCountMap = new HashMap<>();
		int boqcount = boqList.size();
		int lastLevelCount = (int) boqList.stream()
				.filter(BOQ::isLastLevel)
				.count();
		boqCountMap.put("totalBOQCount", boqcount);
		boqCountMap.put("lastLevelBOQCount", lastLevelCount);
		return boqCountMap;
	}
	@Override
	public double findTotalAmountForParentBoqs(String boqId) {
	  
	    return 0;
	}
   
	
	@Override
	public void deleteBOQ(List<String> boqCode, String projectId) throws DataIntegrityViolationException {
		for (String code : boqCode) {
			BOQ boq = boqDao.findById(convertToUUID(code)).orElseThrow(() -> new DataNotFoundException(
					messageSource.getMessage("boq.not.found", null, Locale.ENGLISH)));
            boqDao.delete(boq);
        }
	}
	@Override
	public BOQ getBoqById(String boqId) {
		return boqDao.findById(convertToUUID(boqId))
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("boq.not.found", null, Locale.ENGLISH)));
	}
	@Override
	public void generateBoqExcel(String projectId, HttpServletResponse response)
			throws IOException, DataNotFoundException {
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		List<BOQ> boqList = getAllBOQ(projectId);
		excelService.generateBoqFile(boqList, response);
	}
	private List<BOQ> getAllBOQ(String projectId) {
        return boqDao.findByProjectId(UUID.fromString(projectId));
	}
	@Override
	public void generateBoqPdf(String projectId, HttpServletResponse response)
			throws IOException, DocumentException, DataNotFoundException {
		response.setContentType("application/pdf");
		List<BOQ> boqList = getAllBOQ(projectId);
		pdfService.generateBoqPdf(boqList, response);
	}
	@Override
	public List<BOQ> getParentBOQ(List<BOQ> boqList) {
		if (boqList != null && !boqList.isEmpty()) {
			return boqList.stream().filter(boq -> boq.getParentBOQ() == null).collect(Collectors.toList());
		}
		return null;
	}
	@Override
	public Map<String, Double> getParentBoqTotalMap(String projectId) {
		List<BOQ> boqList = boqDao.findByProjectId(UUID.fromString(projectId));
		if (boqList == null || boqList.isEmpty()) {
			throw new DataNotFoundException("No BOQ found for project");
		}
		Map<String, List<BOQ>> childMap = buildChildMap(boqList);
		Map<String, Double> result = new HashMap<>();
		for (BOQ boq : boqList) {
			if (boq.getParentBOQ() == null) {
				double total = calculateTotalRecursively(boq, childMap);
				result.put(boq.getId().toString(), total);
			}
		}
		return result;
	}
	private Map<String, List<BOQ>> buildChildMap(List<BOQ> boqList) {
		Map<String, List<BOQ>> childMap = new HashMap<>();
		for (BOQ boq : boqList) {
			if (boq.getParentBOQ() != null) {
				String parentId = boq.getParentBOQ().getId().toString();
				childMap
						.computeIfAbsent(parentId, k -> new ArrayList<>())
						.add(boq);
			}
		}
		return childMap;
	}
	private double calculateTotalRecursively(BOQ boq,
											 Map<String, List<BOQ>> childMap) {
		String boqId = boq.getId().toString();
		if (!childMap.containsKey(boqId)) {
			return boq.getTotalAmount() != 0.0 ? boq.getTotalAmount() : 0.0;
		}
		double total = 0.0;
		for (BOQ child : childMap.get(boqId)) {
			total += calculateTotalRecursively(child, childMap);
		}
		return total;
	}
	@Override
	public String saveTemplate(Template template, Map<String, String> mapping) {
		if (templateDao.findByTemplateName(template.getTemplateName()) == null) {
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
        return templateDao.findAll();
	}
	@Override
	public TemplateDto getTemplateById(String templateId) {
		TemplateDto templateDto = new TemplateDto();
		Template template = templateDao.findById(convertToUUID(templateId)).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		templateDto.setTemplate(template);
		List<TemplateMapping> mappings = templateMappingDao.findByTemplateId(UUID.fromString(templateId));
		Map<String, String> mapping = new HashMap<>();
		for (TemplateMapping map : mappings) {
			mapping.put(map.getInternalFields(), map.getExcelFields());
		}
		templateDto.setTemplateMapping(mapping);
		return templateDto;
	}
	@Override
	public List<ProjectInfoDto> getAllBoqUploadedProject() {
		List<ProjectInfoDto> projects = getAllProjects();
		Set<String> projectIds = boqDao.findAll().stream()
				.map(boq -> boq.getProject().getId().toString())
				.collect(Collectors.toSet());
		projects = projects.stream()
				.filter(project -> projectIds.contains(project.getProjectId()))
				.collect(Collectors.toList());
		return projects;
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
	@Transactional
	@Override
	public String addResourcesToTender(TenderResourceDto dto) {
		if (dto.getProjectId() == null) throw new RuntimeException("ProjectId missing");
		if (dto.getBoqId() == null) throw new RuntimeException("BOQ Id missing");
		Projects project = projectDao.findById(convertToUUID(dto.getProjectId()))
				.orElseThrow(() -> new RuntimeException("Project not found"));
		TenderEstimation parent = null;
		BOQ boq;
		if (dto.isInternal()) {
			InternalBoq internalBoq = internalBoqDao.findById(convertToUUID(dto.getBoqId()))
					.orElseThrow(() -> new RuntimeException("Internal BOQ not found"));
			if (internalBoq.getParentResource() == null || internalBoq.getParentResource().isEmpty()) {
				throw new RuntimeException("Internal BOQ has no parent resource");
			}
			parent = internalBoq.getParentResource().get(0);
			boq = parent.getBoq();
		} else {
			boq = boqDao.findById(convertToUUID(dto.getBoqId()))
					.orElseThrow(() -> new RuntimeException("BOQ not found"));
		}
		UUID resourceId = convertToUUID(dto.getResourceId());
		UUID uomId = convertToUUID(dto.getUomId());
		List<UUID> attributeIds = dto.getAttributes() != null
				? dto.getAttributes().stream().map(a -> convertToUUID(a.getAttributeId())).toList()
				: new ArrayList<>();
		Optional<TenderEstimation> duplicateOpt =
				findDuplicateResource(
						parent != null ? parent.getId() : null,
						boq.getId(),
						resourceId,
						uomId,
						attributeIds
				);
		TenderEstimation tenderEst;
		if (duplicateOpt.isPresent()) {
			tenderEst = duplicateOpt.get();
			if (tenderEst.getResourceNature() == ResourceNature.SIMPLE) {
				throw new DuplicateEntryException("This simple resource already exists in this BOQ.");
			}
			double newQty = tenderEst.getNetQuantity() + dto.getNetQuantity();
			tenderEst.setNetQuantity(newQty);
			double totalCost = newQty * tenderEst.getCostUnitRate();
			tenderEst.setTotalCostCompanyCurrency(totalCost);
			tenderEst.setResourceTotalCost(totalCost * tenderEst.getExchangeRate());
		} else {
			tenderEst = new TenderEstimation();
			tenderEst.setProject(project);
			tenderEst.setBoq(boq);
			tenderEst.setParent(parent);
			updateTenderEstimationFields(tenderEst, dto);
			int maxRefCode;
			if (parent == null) {
				maxRefCode = tenderEstDao.findByBoqId(boq.getId()).stream()
						.filter(e -> e.getParent() == null)
						.mapToInt(TenderEstimation::getRefCode)
						.max().orElse(0);
			} else {
				maxRefCode = tenderEstDao.findByParent_Id(parent.getId()).stream()
						.mapToInt(TenderEstimation::getRefCode)
						.max().orElse(0);
			}
			tenderEst.setRefCode(maxRefCode + 1);
			saveFormulaElements(tenderEst, dto);
			if (tenderEst.getResourceNature() == ResourceNature.COMPLEX) {
				double qty;
				if (tenderEst.getParent() == null) {
					qty = boq.getQuantity() * tenderEst.getCoEfficient();
				} else {
					qty = tenderEst.getCoEfficient();
				}
				tenderEst.setCalculatedQuantity(qty);
				tenderEst.setNetQuantity(qty);
				double totalCost = qty * tenderEst.getCostUnitRate();
				tenderEst.setTotalCostCompanyCurrency(totalCost);
				tenderEst.setResourceTotalCost(totalCost * tenderEst.getExchangeRate());
			} else {
				calculateValues(tenderEst, dto);
			}
		}
		tenderEst = tenderEstDao.save(tenderEst);
		if (tenderEst.getResourceNature() == ResourceNature.COMPLEX) {
			syncInternalBoqFromEstimation(tenderEst);
		}
		propagateCost(tenderEst);
		return "Saved Successfully";
	}
	private Optional<TenderEstimation> findDuplicateResource(
			UUID parentId,
			UUID boqId,
			UUID resourceId,
			UUID uomId,
			List<UUID> attributeIds
	) {
		List<TenderEstimation> candidates;
		if (parentId != null) {
			candidates = tenderEstDao.findByParent_IdAndResources_IdAndUom_Id(
					parentId, resourceId, uomId
			);
		} else {
			candidates = tenderEstDao.findByBoq_IdAndParentIsNullAndResources_IdAndUom_Id(
					boqId, resourceId, uomId
			);
		}
		for (TenderEstimation est : candidates) {
			if (isSameTenderAttributes(est, attributeIds)) {
				return Optional.of(est);
			}
		}

		return Optional.empty();
	}
	private void syncInternalBoqFromEstimation(TenderEstimation est) {
		if (est.getResourceNature() != ResourceNature.COMPLEX) return;
		InternalBoq boq = findOrCreateInternalBoq(est);
		if (!boq.getParentResource().contains(est)) {
			boq.getParentResource().add(est);
		}
		double totalQty = boq.getParentResource()
				.stream()
				.mapToDouble(e -> e.getNetQuantity() != 0 ? e.getNetQuantity() : 0)
				.sum();
		double totalAmount = boq.getParentResource()
				.stream()
				.mapToDouble(TenderEstimation::getTotalCostCompanyCurrency)
				.sum();
		boq.setTotalQuantity(totalQty);
		boq.setAmount(totalAmount);
		if (totalQty > 0) {
			boq.setRate(totalAmount / totalQty);
		} else {
			boq.setRate(0.0);
		}
		internalBoqDao.save(boq);
	}
	private boolean isSameTenderAttributes(TenderEstimation est, List<UUID> attributeIds){
		List<TenderEstimationAttribute> existingAttrs = est.getEstimationAttributes();
		if (existingAttrs.size() != attributeIds.size()) return false;
		Set<UUID> existingIds = existingAttrs.stream()
				.map(a -> a.getAttribute().getId())
				.collect(Collectors.toSet());
		Set<UUID> incomingIds = new HashSet<>(attributeIds);
		return existingIds.equals(incomingIds);
	}
	private InternalBoq findOrCreateInternalBoq(TenderEstimation est) {
		List<InternalBoq> existingList = internalBoqDao
				.findByProject_IdAndResource_IdAndUom_Id(
						est.getProject().getId(),
						est.getResources().getId(),
						est.getUom().getId()
				);
		for (InternalBoq boq : existingList) {
			if (isSameInternalAttributes(boq, est.getEstimationAttributes())) {
				if (boq.getParentResource() == null) {
					boq.setParentResource(new ArrayList<>());
				}
				if (!boq.getParentResource().contains(est)) {
					boq.getParentResource().add(est);
				}
				return boq;
			}
		}
		InternalBoq newBoq = new InternalBoq();
		newBoq.setProject(est.getProject());
		newBoq.setResource(est.getResources());
		newBoq.setUom(est.getUom());
		newBoq.setAttributeGroup(resolveAttributeGroup(est));
		List<InternalBoqAttribute> attrs = mapAttributes(est);
		for (InternalBoqAttribute attr : attrs) {
			attr.setInternalBoq(newBoq);
		}
		newBoq.setAttributes(attrs);
		List<TenderEstimation> parentList = new ArrayList<>();
		parentList.add(est);
		newBoq.setParentResource(parentList);
		newBoq.setTotalQuantity(est.getNetQuantity());
		newBoq.setAmount(est.getTotalCostCompanyCurrency());
		if (est.getNetQuantity() > 0) {
			newBoq.setRate(est.getTotalCostCompanyCurrency() / est.getNetQuantity());
		} else {
			newBoq.setRate(0.0);
		}
		return internalBoqDao.save(newBoq);
	}
	private AttributeGroup resolveAttributeGroup(TenderEstimation est) {
		if (est.getEstimationAttributes() == null || est.getEstimationAttributes().isEmpty()) {
			return null;
		}
		Set<AttributeGroup> groups = est.getEstimationAttributes().stream()
				.map(attr -> attr.getAttribute().getAttributeGroup())
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		if (groups.isEmpty()) {
			return null;
		}
		if (groups.size() > 1) {
			throw new RuntimeException("Multiple AttributeGroups found for same TenderEstimation");
		}
		return groups.iterator().next();
	}
	private boolean isSameInternalAttributes(
			InternalBoq boq,
			List<TenderEstimationAttribute> attrs
	) {
		if (boq.getAttributes().size() != attrs.size()) return false;

		Set<UUID> boqAttrIds = boq.getAttributes().stream()
				.map(a -> a.getAttribute().getId())
				.collect(Collectors.toSet());

		Set<UUID> incomingAttrIds = attrs.stream()
				.map(a -> a.getAttribute().getId())
				.collect(Collectors.toSet());

		return boqAttrIds.equals(incomingAttrIds);
	}
	private List<InternalBoqAttribute> mapAttributes(TenderEstimation est) {
		List<InternalBoqAttribute> list = new ArrayList<>();
		for (TenderEstimationAttribute attr : est.getEstimationAttributes()) {
			InternalBoqAttribute i = new InternalBoqAttribute();
			i.setAttribute(attr.getAttribute());
			list.add(i);
		}
		return list;
	}
	private void propagateCost(TenderEstimation node) {
		if (node.getResourceNature() == ResourceNature.COMPLEX) {
			InternalBoq boq = findOrCreateInternalBoq(node);
			double total = boq.getParentResource()
					.stream()
					.mapToDouble(TenderEstimation::getTotalCostCompanyCurrency)
					.sum();

			node.setTotalCostCompanyCurrency(total);

			if (node.getNetQuantity() > 0) {
				node.setCostUnitRate(total / node.getNetQuantity());
			} else {
				node.setCostUnitRate(0);
			}

			node.setResourceTotalCost(total * node.getExchangeRate());

			tenderEstDao.save(node);
		}

		// 🔁 PROPAGATE UP
		if (node.getParent() != null) {
			propagateCost(node.getParent());
		} else {
			updateBoq(node.getBoq());
			updateProject(node.getProject());
		}
	}
	private void updateBoq(BOQ boq) {
		double total = tenderEstDao.findByBoqId(boq.getId()).stream()
				.filter(e -> e.getParent() == null)
				.mapToDouble(TenderEstimation::getTotalCostCompanyCurrency)
				.sum();
		boq.setTotalAmount(total);
		if (boq.getQuantity() > 0) {
			boq.setTotalRate(total / boq.getQuantity());
		}
		boqDao.save(boq);
	}
	private void updateProject(Projects project) {
		List<BOQ> boqList = boqDao.findByProjectId(project.getId());
		double total = boqList.stream()
				.mapToDouble(BOQ::getTotalAmount)
				.sum();
		project.setTotalEstimatedValue(BigDecimal.valueOf(total));
		projectDao.save(project);
	}
	private void calculateValues(TenderEstimation t, TenderResourceDto dto) {

		if (t.getResourceNature() == ResourceNature.COMPLEX) return;

		double calculatedQty;

		if (t.getParent() == null) {
			if (t.getQuantityType() == QuantityType.FORMULA) {
				calculatedQty = evaluateFormula(t.getFormulaElements());
			} else if (t.getQuantityType() == QuantityType.DIRECT) {
				calculatedQty = t.getBoq().getQuantity();
			} else {
				calculatedQty = t.getBoq().getQuantity() * t.getCoEfficient();
			}
		} else {
			calculatedQty = t.getQuantityType() == QuantityType.FORMULA
					? evaluateFormula(t.getFormulaElements())
					: t.getCoEfficient();
		}

		double wasteQty = (dto.getWastePercentage() / 100) * calculatedQty;
		double netQty = calculatedQty + wasteQty;

		double totalCost = netQty * dto.getCostUnitRate();

		t.setCalculatedQuantity(calculatedQty);
		t.setWasteQuantity(wasteQty);
		t.setNetQuantity(netQty);
		t.setTotalCostCompanyCurrency(totalCost);
		t.setResourceTotalCost(totalCost * dto.getExchangeRate());
	}
	private double evaluateFormula(List<TenderFormulaElement> elements) {
		if (elements == null || elements.isEmpty()) return 0;
		elements.sort(Comparator.comparingInt(TenderFormulaElement::getSequence));
		List<Double> values = new ArrayList<>();
		List<String> operators = new ArrayList<>();
		for (TenderFormulaElement el : elements) {
			switch (el.getType()) {
				case SOURCE:
					double value = 0;
					if (el.getSourceType() == FormulaSourceType.BOQ && el.getBoq() != null) {
						value = el.getBoq().getQuantity();
					} else if (el.getSourceType() == FormulaSourceType.RESOURCE && el.getResource() != null) {
						value = el.getResource().getCalculatedQuantity();
					} else if (el.getSourceType() == FormulaSourceType.GLOBAL && el.getGlobalValue() != null) {
						value = el.getGlobalValue().getValue();
					}
					values.add(value);
					break;
				case NUMBER:
					values.add(el.getValue());
					break;
				case OPERATOR:
					operators.add(el.getOperator());
					break;
			}
		}
		double result = values.get(0);
		for (int i = 0; i < operators.size(); i++) {
			String op = operators.get(i);
			double next = values.get(i + 1);
			switch (op) {
				case "+": result += next; break;
				case "-": result -= next; break;
				case "*": result *= next; break;
				case "/": result = next != 0 ? result / next : 0; break;
			}
		}
		return result;
	}
	private void saveFormulaElements(TenderEstimation tenderEst, TenderResourceDto dto) {
		tenderEst.getFormulaElements().clear();
		if (dto.getFormula() == null || dto.getFormula().isEmpty()) {
			return;
		}
		for (FormulaElementDto f : dto.getFormula()) {
			TenderFormulaElement el = new TenderFormulaElement();
			el.setTenderEstimation(tenderEst);
			el.setSequence(f.getSequence());
			el.setType(FormulaElementType.valueOf(f.getType()));
			el.setSourceType(
					f.getSourceType() != null
							? FormulaSourceType.valueOf(f.getSourceType())
							: null
			);
			if ("BOQ".equalsIgnoreCase(f.getSourceType())) {
				el.setBoq(boqDao.findById(convertToUUID(f.getRefId()))
						.orElseThrow(() -> new RuntimeException("BOQ not found")));
			}
			else if ("RESOURCE".equalsIgnoreCase(f.getSourceType())) {
				el.setResource(tenderEstDao.findById(convertToUUID(f.getRefId()))
						.orElseThrow(() -> new RuntimeException("Resource not found")));
			}
			else if ("GLOBAL".equalsIgnoreCase(f.getSourceType())) {
				el.setGlobalValue(globalValueDao.findById(convertToUUID(f.getRefId()))
						.orElseThrow(() -> new RuntimeException("Global value not found")));
			}
			if ("OPERATOR".equalsIgnoreCase(f.getType())) {
				el.setOperator(f.getOperator());
			} else {
				el.setValue(f.getValue());
			}
			tenderEst.getFormulaElements().add(el);
		}
	}
	private void updateTenderEstimationFields(TenderEstimation tenderEst, TenderResourceDto dto) {
		tenderEst.setDocNumber(dto.getDocNumber());
		tenderEst.setCoEfficient(dto.getCoEfficient());
		// 🔥 Fix: Removed tenderEst.setRefCode(dto.getRefCode()); to prevent 0 override
		tenderEst.setWastePercentage(dto.getWastePercentage());
		tenderEst.setRate(dto.getRate());
		tenderEst.setAdditionalRate(dto.getAdditionalRate());
		tenderEst.setShippingPrice(dto.getShippingPrice());
		tenderEst.setCostUnitRate(dto.getCostUnitRate());
		tenderEst.setRateLock(dto.isRateLock());
		tenderEst.setExchangeRate(dto.getExchangeRate());

		ResourceType resourceType = (dto.getResourceTypeId() != null && !dto.getResourceTypeId().isEmpty())
				? ResourceType.valueOf(dto.getResourceTypeId())
				: null;
		tenderEst.setResourceType(resourceType);

		Projects project = (dto.getProjectId() != null && !dto.getProjectId().trim().isEmpty() )
				? findByProjectId(dto.getProjectId())
				: null;
		tenderEst.setProject(project);
		Resources resource = (dto.getResourceId() != null && !dto.getResourceId().trim().isEmpty())
				? masterService.findResourceById(dto.getResourceId())
				: null;
		tenderEst.setResources(resource);
		Uom uom = (dto.getUomId() != null && !dto.getUomId().trim().isEmpty())
				? masterService.findUomById(dto.getUomId())
				: null;
		tenderEst.setUom(uom);
		ResourceNature resourceNature = (dto.getResourceNatureId() != null && !dto.getResourceNatureId().isEmpty())
				? ResourceNature.valueOf(dto.getResourceNatureId().toUpperCase())
				: null;
		tenderEst.setResourceNature(resourceNature);
		QuantityType quantityType = null;
		if (dto.getQuantityTypeId() != null && !dto.getQuantityTypeId().trim().isEmpty()) {
			quantityType = QuantityType.valueOf(dto.getQuantityTypeId().toUpperCase());
		}
		tenderEst.setQuantityType(quantityType);
		Currency currency = (dto.getCurrencyId() != null && !dto.getCurrencyId().trim().isEmpty())
				? masterService.findCurrencyById(dto.getCurrencyId())
				: null;
		tenderEst.setCurrency(currency);
		if (dto.getAttributes() != null && !dto.getAttributes().isEmpty()) {
			tenderEst.getEstimationAttributes().clear();
			List<TenderEstimationAttribute> attributes = new ArrayList<>();
			for (AttributeSelectionDto d : dto.getAttributes()) {
				TenderEstimationAttribute attribute = new TenderEstimationAttribute();
				attribute.setTenderEstimation(tenderEst);
				attribute.setAttributeGroup(
						attributeGroupDao.findById(convertToUUID(d.getAttributeGroupId()))
								.orElseThrow(() -> new RuntimeException("Attribute Group not found"))
				);
				attribute.setAttribute(
						attributeDao.findById(convertToUUID(d.getAttributeId()))
								.orElseThrow(() -> new RuntimeException("Attribute not found"))
				);
				attributes.add(attribute);
			}
			tenderEst.setEstimationAttributes(attributes);
		}
	}
	@Override
	public List<InternalBoq> getInternalBoqByProject(UUID projectId) {
		Projects project = projectDao.findById(projectId)
				.orElseThrow(() -> new RuntimeException("Project not found"));
		return internalBoqDao.findByProject(project);
	}
	@Override
	public ActivityGroup getActivityGroupById(String activityGroupId) {
		return activityGroupsDao.findById(convertToUUID(activityGroupId)).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	}
	@Override
	public List<TenderEstResourceDto> getEstimatedResourcesByBoq(String boqId) {
		UUID boqUUID = convertToUUID(boqId);
		List<TenderEstimation> estimations = tenderEstDao.findByBoqId(boqUUID);
		List<TenderEstResourceDto> dtoList = new ArrayList<>();
		for (TenderEstimation est : estimations) {
			TenderEstResourceDto dto = new TenderEstResourceDto();
			dto.setTenderEstimation(est);
			dto.setCalculatedQuantity(est.getCalculatedQuantity());
			dto.setWasteQuantity(est.getWasteQuantity());
			dto.setNetQuantity(est.getNetQuantity());
			dto.setTotalCostCompanyCurrency(est.getTotalCostCompanyCurrency());
			dto.setResourceTotalCost(est.getResourceTotalCost());
			dto.setCoEfficient(est.getCoEfficient());
			dtoList.add(dto);
		}
		return dtoList;
	}
	@Override
	public InternalBoq getInternalBoqById(String internalBoqId) {
		return  internalBoqDao.findById(convertToUUID(internalBoqId))
				.orElseThrow(()->new DataNotFoundException(
						messageSource.getMessage("data.not.found",null, Locale.ENGLISH)));
	}
	@Override
	@Transactional
	public String deleteTenderEstimationForBoq(String tenderEstimationId, String boqId) {
		UUID tenderId = convertToUUID(tenderEstimationId);
		UUID boqUUID = convertToUUID(boqId);
		TenderEstimation tenderEstimation = tenderEstDao.findById(tenderId)
				.orElseThrow(() -> new RuntimeException("TenderEstimation not found"));
		if (tenderEstimation.getBoq() == null ||
				!tenderEstimation.getBoq().getId().equals(boqUUID)) {
			return messageSource.getMessage("tender.resource.not.associated", null, Locale.ENGLISH);
		}
		BOQ boq = tenderEstimation.getBoq();
		TenderEstimation parent = tenderEstimation.getParent();
		List<TenderEstimation> children = tenderEstDao.findByParent_Id(tenderEstimation.getId());
		for (TenderEstimation child : children) {
			deleteTenderEstimationForBoq(child.getId().toString(), boqId);
		}
		List<InternalBoq> internalBoqs =
				internalBoqDao.findByProject_IdAndResource_IdAndUom_Id(
						tenderEstimation.getProject().getId(),
						tenderEstimation.getResources().getId(),
						tenderEstimation.getUom().getId()
				);
		for (InternalBoq iBoq : internalBoqs) {
			if (!isSameInternalAttributes(iBoq, tenderEstimation.getEstimationAttributes())) {
				continue;
			}
			iBoq.getParentResource().removeIf(
					est -> est.getId().equals(tenderEstimation.getId())
			);
			if (iBoq.getParentResource().isEmpty()) {
				internalBoqDao.delete(iBoq);
			} else {
				double totalQty = iBoq.getParentResource().stream()
						.mapToDouble(TenderEstimation::getNetQuantity)
						.sum();
				double totalAmount = iBoq.getParentResource().stream()
						.mapToDouble(TenderEstimation::getTotalCostCompanyCurrency)
						.sum();
				iBoq.setTotalQuantity(totalQty);
				iBoq.setAmount(totalAmount);
				if (totalQty > 0) {
					iBoq.setRate(totalAmount / totalQty);
				} else {
					iBoq.setRate(0.0);
				}
				internalBoqDao.save(iBoq);
			}
		}
		if (tenderEstimation.getResourceNature() == ResourceNature.COMPLEX) {
			List<InternalBoq> boqs =
					internalBoqDao.findByProject_IdAndResource_IdAndUom_Id(
							tenderEstimation.getProject().getId(),
							tenderEstimation.getResources().getId(),
							tenderEstimation.getUom().getId()
					);

			for (InternalBoq iBoq : boqs) {
				if (isSameInternalAttributes(iBoq, tenderEstimation.getEstimationAttributes())) {
					internalBoqDao.delete(iBoq);
				}
			}
		}
		tenderEstDao.delete(tenderEstimation);
		if (parent != null) {
			propagateCost(parent);
		} else {
			updateBoq(boq);
			updateProject(boq.getProject());
		}
		return messageSource.getMessage("tender.resource.delete.success", null, Locale.ENGLISH);
	}
	@Override
	public TenderEstResourceDto getEstimatedResourceById(String tenderEstimationId) {
		UUID tenderId = convertToUUID(tenderEstimationId);
		TenderEstimation tenderEstimation = tenderEstDao.findById(tenderId)
				.orElseThrow(() -> new RuntimeException(
						"TenderEstimation not found with ID: " + tenderEstimationId));
		TenderEstResourceDto dto = new TenderEstResourceDto();
		dto.setTenderEstimation(tenderEstimation);
		dto.setCalculatedQuantity(tenderEstimation.getCalculatedQuantity());
		dto.setWasteQuantity(tenderEstimation.getWasteQuantity());
		dto.setNetQuantity(tenderEstimation.getNetQuantity());
		dto.setTotalCostCompanyCurrency(tenderEstimation.getTotalCostCompanyCurrency());
		dto.setResourceTotalCost(tenderEstimation.getResourceTotalCost());
		dto.setCoEfficient(tenderEstimation.getCoEfficient());
		return dto;
	}
	@Override
	public TenderEstResourceDto getEstimatedResourceByIdAndBoqId(String tenderEstimationId, String boqId) {
		UUID tenderId = convertToUUID(tenderEstimationId);
		UUID boqUUID = convertToUUID(boqId);
		TenderEstimation tenderEstimation = tenderEstDao.findById(tenderId)
				.orElseThrow(() -> new RuntimeException(
						"TenderEstimation not found with ID: " + tenderEstimationId));
		if (tenderEstimation.getBoq() == null ||
				!tenderEstimation.getBoq().getId().equals(boqUUID)) {
			throw new RuntimeException(
					"TenderEstimation does not belong to BOQ: " + boqId);
		}
		TenderEstResourceDto dto = new TenderEstResourceDto();
		dto.setTenderEstimation(tenderEstimation);
		dto.setCalculatedQuantity(tenderEstimation.getCalculatedQuantity());
		dto.setWasteQuantity(tenderEstimation.getWasteQuantity());
		dto.setNetQuantity(tenderEstimation.getNetQuantity());
		dto.setTotalCostCompanyCurrency(tenderEstimation.getTotalCostCompanyCurrency());
		dto.setResourceTotalCost(tenderEstimation.getResourceTotalCost());
		dto.setCoEfficient(tenderEstimation.getCoEfficient());
		return dto;
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
			int maxExistingVersion = offerQuotations.size();
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
			currentVersion = 1;
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
		return tenderOffersDao.findByTenderId(UUID.fromString(tenderId));
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
		TenderOffers offers = tenderOffersDao.findByTenderIdAndContractorId(UUID.fromString(tenderId),UUID.fromString(contractorId)).
				orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("tender.offer.not.found",null,Locale.ENGLISH)));
		List<OfferQuotation> quote = offers.getOfferQuotations().stream()
				.filter(offer -> offer.getOfferVersion() == offerVersion)
				.toList();
		Tender tender = offers.getTender();
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
	@Override
	public List<BOQ> searchBoq(String projectId, String searchQuery) {
		UUID uuid = UUID.fromString(projectId);
		List<BOQ> boqList = boqDao.findByProjectId(uuid);
		if (searchQuery == null || searchQuery.trim().isEmpty()) {
			return boqList;
		}
		String search = searchQuery.toLowerCase();
		return boqList.stream()
				.filter(boq ->
						(boq.getBoqName() != null && boq.getBoqName().toLowerCase().contains(search)) ||
								(boq.getBoqCode() != null && boq.getBoqCode().toLowerCase().contains(search))
				)
				.toList();
	}

	@Override
	public List<GlobalValue> viewAllGlobalValueByProjectId(String projectId) {
		return globalValueDao.findByProject_Id(convertToUUID(projectId));
	}

	@Override
	public String addGlobalValue(String projectId, GlobalValue globalValue) {
		UUID projId = convertToUUID(projectId);
		if (globalValueDao.existsByProject_IdAndNameIgnoreCase(projId, globalValue.getName())) {
			throw new RuntimeException("Global Value name already exists for this project");
		}
		if (globalValueDao.existsByProject_IdAndCodeIgnoreCase(projId, globalValue.getCode())) {
			throw new RuntimeException("Global Value code already exists for this project");
		}
		Projects project = findByProjectId(projectId);
		globalValue.setProject(project);
		globalValueDao.save(globalValue);
		return "Global Value added successfully";
	}
	@Override
	public String editGlobalValue(String projectId, GlobalValue globalValue) {
		UUID projId = convertToUUID(projectId);
		GlobalValue existing = globalValueDao.findById(globalValue.getId())
				.orElseThrow(() -> new RuntimeException("Global Value not found"));
		if (globalValueDao.existsByProject_IdAndNameIgnoreCaseAndIdNot(
				projId, globalValue.getName(), globalValue.getId())) {
			throw new RuntimeException("Global Value name already exists for this project");
		}
		if (globalValueDao.existsByProject_IdAndCodeIgnoreCaseAndIdNot(
				projId, globalValue.getCode(), globalValue.getId())) {
			throw new RuntimeException("Global Value code already exists for this project");
		}
		existing.setName(globalValue.getName());
		existing.setCode(globalValue.getCode());
		existing.setValue(globalValue.getValue());
		globalValueDao.save(existing);
		return "Global Value updated successfully";
	}
}