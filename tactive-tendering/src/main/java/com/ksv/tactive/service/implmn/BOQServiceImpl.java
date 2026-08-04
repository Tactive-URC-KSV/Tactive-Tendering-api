package com.ksv.tactive.service.implmn;

import com.itextpdf.text.DocumentException;
import com.ksv.tactive.dto.ExcelExtractBOQ;
import com.ksv.tactive.dto.ProjectInfoDto;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.exception.FileNotSaveException;
import com.ksv.tactive.fileservice.ExcelFileService;
import com.ksv.tactive.fileservice.PdfFileService;
import com.ksv.tactive.project.dao.BOQDao;
import com.ksv.tactive.project.dao.BOQFileDao;
import com.ksv.tactive.project.dao.ProjectDao;
import com.ksv.tactive.project.dao.TemplateDao;
import com.ksv.tactive.project.dao.TemplateMappingDao;
import com.ksv.tactive.project.entity.*;
import com.ksv.tactive.dto.TemplateDto;
import com.ksv.tactive.service.BOQService;
import com.ksv.tactive.service.MasterService;
import com.ksv.tactive.service.ProjectService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BOQServiceImpl implements BOQService {

    @Value("${file.upload.path}")
    private String uploadDir;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private BOQDao boqDao;

    @Autowired
    private BOQFileDao boqFileDao;

    @Autowired
    private ExcelFileService excelService;

    @Autowired
    private PdfFileService pdfService;

    @Autowired
    private MasterService masterService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TemplateDao templateDao;

    @Autowired
    private TemplateMappingDao templateMappingDao;

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
    public List<ProjectInfoDto> getAllBoqUploadedProject() {
        Set<String> projectIds = boqDao.findAll().stream()
                .filter(bq -> bq.getProjectId().length() == 36)
                .map(BOQ::getProjectId)
                .collect(Collectors.toSet());
        return projectService.getAllProjects().stream()
                .filter(project -> projectIds.contains(UUID.fromString(project.getProjectId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getExcelSheets(MultipartFile boqFile) throws IOException {
        return excelService.getSheetNames(boqFile);
    }

    @Override
    public List<String> getPdfPages(MultipartFile boqFile) throws IOException {
        return pdfService.getPdfPages(boqFile);
    }

    @Override
    public List<String> getPdfColumns(MultipartFile boqFile, int startPage) throws IOException {
        return pdfService.getColumnsHeaders(boqFile, startPage);
    }

    @Override
    public List<String> getExcelColumns(MultipartFile boqFile, String sheetName) throws IOException {
        String fileName = boqFile.getOriginalFilename();
        if (fileName != null && (fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))) {
            return excelService.getColumns(boqFile, sheetName);
        } else if (fileName != null && fileName.endsWith(".pdf")) {
            int startPage = 1;
            try {
                startPage = Integer.parseInt(sheetName);
            } catch (NumberFormatException ignored) {}
            return getPdfColumns(boqFile, startPage);
        }
        return null;
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
            List<BOQFile> boqFiles = boqFileDao.findByProject(projectId);
            if (boqFiles.stream().noneMatch(b -> b.getFilename().equals(fileName))) {
                BOQFile boqFileEntity = new BOQFile();
                boqFileEntity.setFilename(fileName);
                boqFileEntity.setFileUrl(filePath);
                boqFileEntity.setProject(projectId);
                boqFileDao.save(boqFileEntity);
            }
            return filePath;
        } catch (IOException e) {
            throw new FileNotSaveException(messageSource.getMessage("file.upload.failure", null, Locale.ENGLISH), e);
        }
    }

    @Override
    public String mapBOQData(String projectId, String sheetName, Map<String, List<String>> columnMapping,
                             Map<Long, Long> parentChildMapping, Map<Long, Boolean> lastLevelMapping, Map<Long, Long> levelMapping,
                             List<Long> ignoredSnoList, MultipartFile boqFile, String pageBreakWord) throws IOException {
//        Projects project = projectDao.findById(convertToUUID(projectId)).orElse(null);
//        saveBoqFile(boqFile, projectId);
        int startRow = 0;
        int endRow = 100000;
        List<ExcelExtractBOQ> extractedRows;
        String fileName = boqFile.getOriginalFilename();
        if (fileName != null && (fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))) {
            extractedRows = excelService.getExtractedData(boqFile, sheetName, columnMapping, startRow, endRow, pageBreakWord);
        } else {
            int startPdfPage = 1;
            try { startPdfPage = Integer.parseInt(sheetName); } catch (Exception e) {}
            extractedRows = pdfService.getExtractedData(boqFile, columnMapping, startRow, endRow, startPdfPage, pageBreakWord);
        }
        if (extractedRows == null || extractedRows.isEmpty()) {
            throw new DataNotFoundException("No BOQ data extracted from file!");
        }
        Map<String, BOQ> boqUniqueMap = new LinkedHashMap<>();
        Map<Long, BOQ> rowToBoqMap = new HashMap<>();
        long currentTime = System.currentTimeMillis();
        for (ExcelExtractBOQ row : extractedRows) {
            if (ignoredSnoList != null && ignoredSnoList.contains(row.getSno())) {
                continue;
            }
            String safeBoqCode = row.getBoqCode() == null ? "" : row.getBoqCode();
            String safeBoqName = row.getBoqName() == null ? "" : row.getBoqName();
            String key = (safeBoqCode + "_" + safeBoqName).toLowerCase();
            if (safeBoqCode.trim().isEmpty() && safeBoqName.trim().isEmpty()) {
                key += "_" + row.getSno();
            }

            BOQ boq = boqUniqueMap.get(key);
            if (boq == null) {
                if (safeBoqCode.trim().isEmpty() && safeBoqName.trim().isEmpty()) {
                    boq = new BOQ();
                } else {
                    boq = boqDao
                            .findByProjectIdAndBoqCodeIgnoreCaseAndBoqNameIgnoreCase(
                                    projectId,
                                    safeBoqCode,
                                    safeBoqName
                            )
                            .orElseGet(BOQ::new);
                }
                if (boq.getId() == null) {
                    boq.setProjectId(projectId);
                }
                boq.setBoqCode(row.getBoqCode());
                boq.setBoqName(row.getBoqName());
                if (row.getUom() != null) {
                    boq.setUom(findUOM(row.getUom()));
                    boq.setUomCode(boq.getUom() != null ? boq.getUom().getUomCode() : row.getUom());
                }
                if (boq.getCreatedAt() == null) {
                    boq.setCreatedAt(new java.sql.Timestamp(currentTime++));
                }
                boq.setDivision(row.getDivision());
                boqUniqueMap.put(key, boq);
            }
            boq.setQuantity(row.getQuantity() != null ? row.getQuantity() : 0.0);
            Long level = levelMapping.getOrDefault(row.getSno(), 0L);
            boq.setLevel(level.intValue());
            boq.setPageNo(row.getPageNo());
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
    public List<BOQ> getParentBOQ(String projectId) {
        List<BOQ> boqList = boqDao.findByProjectIdOrderByCreatedAtAsc(projectId);
        if (boqList.isEmpty()) {
            return null;
        }
        return boqList.stream()
                .filter(boq -> boq.getLevel() == 1)
                .collect(Collectors.toList());
    }

    @Override
    public List<BOQ> getChildBOQ(String projectId, String parentBoqId) {
        List<BOQ> boqList = boqDao.findByProjectIdAndParentBOQIdOrderByCreatedAtAsc(projectId, UUID.fromString(parentBoqId));
        if (boqList.isEmpty()) {
            return null;
        }
        return boqList;
    }

    @Override
    public List<BOQ> getParentBOQ(List<BOQ> boqList) {
        return boqList.stream()
                .filter(boq -> boq.getParentBOQ() == null)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Integer> getBOQCount(String projectId) {
        List<BOQ> boqList = boqDao.findByProjectIdOrderByCreatedAtAsc(projectId);
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
        BOQ boq = boqDao.findById(convertToUUID(boqId)).orElse(null);
        if (boq == null || boq.getParentBOQ() == null) {
            return 0;
        }
        double total = 0;
        BOQ parent = boq.getParentBOQ();
        while (parent != null) {
            total += parent.getTotalAmount();
            parent = parent.getParentBOQ();
        }
        return total;
    }

    @Override
    public void generateBoqExcel(String projectId, HttpServletResponse response) throws IOException, DataNotFoundException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        List<BOQ> boqList = boqDao.findByProjectIdOrderByCreatedAtAsc(projectId);
        excelService.generateBoqFile(boqList, response);
    }

    @Override
    public void generateBoqPdf(String projectId, HttpServletResponse response) throws IOException, DocumentException, DataNotFoundException {
        response.setContentType("application/pdf");
        List<BOQ> boqList = boqDao.findByProjectIdOrderByCreatedAtAsc(projectId);
        pdfService.generateBoqPdf(boqList, response);
    }

    @Override
    @Transactional
    public void deleteBOQ(List<String> boqCode, String projectId) {
        for (String code : boqCode) {
            BOQ boq = boqDao.findById(convertToUUID(code))
                    .orElseThrow(() -> new DataNotFoundException(
                            messageSource.getMessage("boq.not.found", null, Locale.ENGLISH)));
            deleteChildren(boq);
            boq.setArchive(true);
            boqDao.save(boq);
        }
    }
    private void deleteChildren(BOQ parent) {
        if (parent.getChildren() != null && !parent.getChildren().isEmpty()) {
            List<BOQ> children = new ArrayList<>(parent.getChildren());
            for (BOQ child : children) {
                deleteChildren(child);
                child.setArchive(true);
                boqDao.save(child);
            }
        }
    }
    @Override
    public BOQ getBoqById(String boqId) {
        return boqDao.findById(convertToUUID(boqId))
                .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("boq.not.found", null, Locale.ENGLISH)));
    }

    @Override
    public List<ExcelExtractBOQ> extractBOQData(String sheetName, Map<String, List<String>> columnMapping, MultipartFile boqFile, String search, String pageBreakWord) throws IOException {
        List<ExcelExtractBOQ> allRows;
        int startRow = 0;
        int endRow = 100000;
        String fileName = boqFile.getOriginalFilename();
        if (fileName != null && (fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))) {
            allRows = excelService.getExtractedData(boqFile, sheetName, columnMapping, startRow, endRow, pageBreakWord);
        } else {
            int startPdfPage = 1;
            try { startPdfPage = Integer.parseInt(sheetName); } catch (Exception e) {}
            allRows = pdfService.getExtractedData(boqFile, columnMapping, startRow, endRow, startPdfPage, pageBreakWord);
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
        return allRows;
    }

    @Override
    public List<BOQ> searchBoq(String projectId, String searchQuery) {
        UUID uuid = UUID.fromString(projectId);
        List<BOQ> boqList = boqDao.findByProjectIdOrderByCreatedAtAsc(uuid.toString());
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
    public Map<String, Double> getParentBoqTotalMap(String projectId) {
        List<BOQ> boqList = boqDao.findByProjectIdOrderByCreatedAtAsc(projectId);
        if (boqList == null || boqList.isEmpty()) {
            throw new DataNotFoundException("No BOQ found for project");
        }
        Map<String, List<BOQ>> childMap = buildChildMap(boqList);
        Map<String, Double> result = new HashMap<>();
        
        // Calculate for ALL non-leaf BOQs
        for (BOQ boq : boqList) {
            if (!boq.isLastLevel()) {
                double total = calculateTotalRecursively(boq, childMap);
                result.put(boq.getId().toString(), total);
            }
        }
        
        // Calculate for divisions
        Map<String, List<BOQ>> divisionMap = new HashMap<>();
        for (BOQ boq : boqList) {
            if (boq.getParentBOQ() == null) {
                String div = boq.getDivision();
                if (div == null || div.trim().isEmpty() || "Non-Categorized".equalsIgnoreCase(div)) {
                    div = "Non-Categorized";
                }
                divisionMap.computeIfAbsent(div, k -> new ArrayList<>()).add(boq);
            }
        }
        
        for (Map.Entry<String, List<BOQ>> entry : divisionMap.entrySet()) {
            double divTotal = 0.0;
            for (BOQ boq : entry.getValue()) {
                divTotal += result.getOrDefault(boq.getId().toString(), boq.getTotalAmount() != 0.0 ? boq.getTotalAmount() : 0.0);
            }
            result.put("div_" + entry.getKey(), divTotal);
        }
        
        return result;
    }

    private Map<String, List<BOQ>> buildChildMap(List<BOQ> boqList) {
        Map<String, List<BOQ>> childMap = new HashMap<>();
        for (BOQ boq : boqList) {
            if (boq.getParentBOQ() != null) {
                String parentId = boq.getParentBOQ().getId().toString();
                childMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(boq);
            }
        }
        return childMap;
    }

    private double calculateTotalRecursively(BOQ boq, Map<String, List<BOQ>> childMap) {
        String boqId = boq.getId().toString();
        if (!childMap.containsKey(boqId)) {
            if (!boq.isLastLevel()) {
                return 0.0;
            }
            return boq.getTotalAmount() != 0.0 ? boq.getTotalAmount() : 0.0;
        }
        double total = 0.0;
        for (BOQ child : childMap.get(boqId)) {
            total += calculateTotalRecursively(child, childMap);
        }
        return total;
    }

    @Override
    public String updateBOQHierarchy(String projectId, Map<String, String> parentChildUpdateMap) {
        UUID projId = convertToUUID(projectId);
        List<BOQ> boqList = boqDao.findByProjectIdOrderByCreatedAtAsc(projId.toString());
        if (boqList == null || boqList.isEmpty()) {
            throw new DataNotFoundException("No BOQs found for project");
        }
        Map<UUID, BOQ> boqMap = boqList.stream()
                .collect(Collectors.toMap(BOQ::getId, boq -> boq));
        for (Map.Entry<String, String> entry : parentChildUpdateMap.entrySet()) {

            UUID childId = convertToUUID(entry.getKey());
            String parentValue = entry.getValue();

            BOQ child = boqMap.get(childId);
            if (child == null) {
                throw new RuntimeException("Invalid child BOQ: " + childId);
            }

            // Remove from old parent
            BOQ oldParent = child.getParentBOQ();
            if (oldParent != null) {
                oldParent.getChildren().remove(child);
            }

            // ROOT handling
            if ("ROOT".equalsIgnoreCase(parentValue)) {
                child.setParentBOQ(null);
                continue;
            }

            UUID parentId = convertToUUID(parentValue);

            if (childId.equals(parentId)) {
                throw new RuntimeException("BOQ cannot be parent of itself: " + childId);
            }

            BOQ newParent = boqMap.get(parentId);
            if (newParent == null) {
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

    @Override
    public String updateBOQDivision(String projectId, Map<String, String> boqDivisionUpdateMap) {
        UUID projId = convertToUUID(projectId);
        List<BOQ> boqList = boqDao.findByProjectIdOrderByCreatedAtAsc(projId.toString());
        if (boqList == null || boqList.isEmpty()) {
            throw new DataNotFoundException("No BOQs found for project");
        }
        Map<UUID, BOQ> boqMap = boqList.stream()
                .collect(Collectors.toMap(BOQ::getId, boq -> boq));

        for (Map.Entry<String, String> entry : boqDivisionUpdateMap.entrySet()) {
            UUID childId = convertToUUID(entry.getKey());
            String newDivision = entry.getValue();

            if (newDivision != null && newDivision.startsWith("div_")) {
                newDivision = newDivision.substring(4);
            }
            if ("Non-Categorized".equalsIgnoreCase(newDivision) || "ROOT".equalsIgnoreCase(newDivision)) {
                newDivision = null;
            }

            BOQ child = boqMap.get(childId);
            if (child == null) {
                throw new RuntimeException("Invalid child BOQ: " + childId);
            }
            
            child.setDivision(newDivision);
        }
        boqDao.saveAll(boqList);
        return "BOQ division updated successfully for project: " + projectId;
    }

    @Override
    public Page<BOQ> getParentBOQs(String projectId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<BOQ> boqPage = boqDao.findByProjectIdAndParentBOQIsNull(projectId, pageable);
        if (boqPage.isEmpty()) {
            throw new DataNotFoundException("No BOQ data found");
        }
        return boqPage;
    }

    @Override
    public String saveTemplate(Template template, Map<String, List<String>> mapping) {
        if (templateDao.findByTemplateName(template.getTemplateName()) == null) {
            Template savedTemplate = templateDao.save(template);
            saveTemplateMapping(mapping, savedTemplate);
            return messageSource.getMessage("template.save.success", null, Locale.ENGLISH);
        }
        return messageSource.getMessage("template.duplicate", null, Locale.ENGLISH);
    }

    private void saveTemplateMapping(Map<String, List<String>> mapping, Template savedTemplate) {
        for (Map.Entry<String, List<String>> map : mapping.entrySet()) {
            TemplateMapping tempMap = new TemplateMapping();
            tempMap.setTemplate(savedTemplate);
            tempMap.setInternalFields(map.getKey());
            tempMap.setExcelFields(String.join("|||", map.getValue()));
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
        Map<String, List<String>> mapping = new HashMap<>();
        for (TemplateMapping map : mappings) {
            String excelFields = map.getExcelFields();
            List<String> fieldList = Arrays.asList(excelFields.split("\\|\\|\\|"));
            mapping.put(map.getInternalFields(), fieldList);
        }
        templateDto.setTemplateMapping(mapping);
        return templateDto;
    }

    @Override
    public List<BOQ> getAllBOQsByProjectId(String projectId) {
        List<BOQ> boqList = boqDao.findByProjectIdOrderByCreatedAtAsc(projectId);
        if (boqList == null || boqList.isEmpty()) {
            throw new DataNotFoundException(messageSource.getMessage("boq.not.found", null, Locale.ENGLISH));
        }
        for (BOQ boq : boqList) {
            if (boq.getParentBOQ() != null) {
                boq.setParentBoqId(boq.getParentBOQ().getId().toString());
            }
        }
        return boqList;
    }

    @Override
    public List<String> getDivisionsByProjectId(String projectId) {
        return boqDao.findDistinctDivisionsByProjectId(projectId);
    }

    @Override
    public Page<BOQ> getBOQsByDivision(String projectId, String division, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        if (division == null || division.trim().isEmpty() || division.equalsIgnoreCase("Non-Categorized")) {
            return boqDao.findByProjectIdAndNoDivision(projectId, pageable);
        }
        return boqDao.findByProjectIdAndDivisionOrderByCreatedAtAsc(projectId, division, pageable);
    }

    @Override
    public List<BOQ> getBoqsByLevel(String projectId, int level) {
        return boqDao.findByProjectIdAndLevel(projectId, level);
    }

    @Override
    public BOQ createSingleBoq(String projectId, BOQ boqData) {
        boqData.setProjectId(projectId);
        
        if (boqData.getUomCode() != null && !boqData.getUomCode().trim().isEmpty()) {
            boqData.setUom(findUOM(boqData.getUomCode()));
        }

        if (boqData.getLevel() > 1 && boqData.getParentBoqId() != null && !boqData.getParentBoqId().trim().isEmpty()) {
            BOQ parentBoq = boqDao.findById(UUID.fromString(boqData.getParentBoqId()))
                    .orElseThrow(() -> new DataNotFoundException("Parent BOQ not found"));
            
            parentBoq.setLastLevel(false);
            boqDao.save(parentBoq);
            boqData.setParentBOQ(parentBoq);
            
            if (boqData.getPageNo() == null || boqData.getPageNo() == 0) {
                boqData.setPageNo(parentBoq.getPageNo());
            }
        }
        
        return boqDao.save(boqData);
    }

    @Override
    public BOQ updateSingleBoq(String projectId, String boqId, BOQ boqData) {
        BOQ existingBoq = boqDao.findById(convertToUUID(boqId))
                .orElseThrow(() -> new DataNotFoundException("BOQ not found"));
        
        existingBoq.setBoqCode(boqData.getBoqCode());
        existingBoq.setBoqName(boqData.getBoqName());
        existingBoq.setDescription(boqData.getDescription());
        existingBoq.setQuantity(boqData.getQuantity());
        
        if (boqData.getUomCode() != null && !boqData.getUomCode().trim().isEmpty()) {
            existingBoq.setUomCode(boqData.getUomCode());
            existingBoq.setUom(findUOM(boqData.getUomCode()));
        } else {
            existingBoq.setUomCode(null);
            existingBoq.setUom(null);
        }

        existingBoq.setDivision(boqData.getDivision());
        existingBoq.setPageNo(boqData.getPageNo());
        existingBoq.setEnqSlNo(boqData.getEnqSlNo());
        existingBoq.setClientBoqCode(boqData.getClientBoqCode());
        existingBoq.setBoqShortName(boqData.getBoqShortName());
        existingBoq.setRateOnly(boqData.isRateOnly());
        existingBoq.setProvisionalSum(boqData.isProvisionalSum());
        existingBoq.setNotQuotedBoq(boqData.isNotQuotedBoq());
        existingBoq.setPriceBoq(boqData.isPriceBoq());
        existingBoq.setLastLevel(boqData.isLastLevel());
        
        // Handle level and parent BOQ
        existingBoq.setLevel(boqData.getLevel());
        if (boqData.getLevel() > 1 && boqData.getParentBoqId() != null && !boqData.getParentBoqId().trim().isEmpty()) {
            BOQ parentBoq = boqDao.findById(UUID.fromString(boqData.getParentBoqId()))
                    .orElseThrow(() -> new DataNotFoundException("Parent BOQ not found"));
            parentBoq.setLastLevel(false);
            boqDao.save(parentBoq);
            existingBoq.setParentBOQ(parentBoq);
        } else if (boqData.getLevel() <= 1) {
            existingBoq.setParentBOQ(null);
        }
        
        // Increment version on update
        int currentVersion = existingBoq.getVersion() != null ? existingBoq.getVersion() : 1;
        existingBoq.setVersion(currentVersion + 1);

        return boqDao.save(existingBoq);
    }
}
