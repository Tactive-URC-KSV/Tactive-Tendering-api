package com.ksv.tactive.service;

import com.ksv.tactive.dto.ExcelExtractBOQ;
import com.ksv.tactive.dto.PaginatedResponse;
import com.ksv.tactive.dto.ProjectInfoDto;
import com.ksv.tactive.project.entity.BOQ;
import com.ksv.tactive.project.entity.Template;
import com.ksv.tactive.dto.TemplateDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import com.itextpdf.text.DocumentException;
import com.ksv.tactive.exception.DataNotFoundException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface BOQService {
    List<ProjectInfoDto> getAllBoqUploadedProject();
    List<String> getExcelSheets(MultipartFile boqFile) throws IOException;
    List<String> getPdfPages(MultipartFile boqFile) throws IOException;
    List<String> getPdfColumns(MultipartFile boqFile, int startPage) throws IOException;
    List<String> getExcelColumns(MultipartFile boqFile, String sheetName) throws IOException;
    String saveBoqFile(MultipartFile boqFile, String projectId);
    boolean checkFileExists(String projectId, String fileName);
    String mapBOQData(String projectId, String sheetName, Map<String, List<String>> columnMapping, Map<Long, Long> parentChildMapping, Map<Long, Boolean> lastLevelMapping, Map<Long, Long> levelMapping, List<Long> ignoredSnoList, List<Long> saveAsNewSnoList, MultipartFile boqFile, String pageBreakWord) throws IOException;
    List<BOQ> getParentBOQ(String projectId);
    List<BOQ> getChildBOQ(String projectId, String parentBoqId);
    List<BOQ> getParentBOQ(List<BOQ> boqList);
    Map<String, Integer> getBOQCount(String projectId);
    double findTotalAmountForParentBoqs(String boqId);
    void generateBoqExcel(String projectId, HttpServletResponse response) throws IOException, DataNotFoundException;
    void generateBoqPdf(String projectId, HttpServletResponse response) throws IOException, DocumentException, DataNotFoundException;
    void deleteBOQ(List<String> boqCode, String projectId);
    BOQ getBoqById(String boqId);
    List<ExcelExtractBOQ> extractBOQData(String sheetName,
                                                            Map<String, List<String>> columnMapping, MultipartFile boqFile, String search, String pageBreakWord) throws IOException;
    List<BOQ> searchBoq(String projectId, String searchQuery);
    Map<String, Double> getParentBoqTotalMap(String projectId);
    String updateBOQHierarchy(String projectId, Map<String, String> parentChildUpdateMap);
    String updateBOQDivision(String projectId, Map<String, String> boqDivisionUpdateMap);
    Page<BOQ> getParentBOQs(String projectId, int page, int size);
    String saveTemplate(Template template, Map<String, List<String>> mapping);
    List<Template> getAllTemplate();
    TemplateDto getTemplateById(String templateId);
    List<BOQ> getAllBOQsByProjectId(String projectId);
    List<String> getDivisionsByProjectId(String projectId);
    Page<BOQ> getBOQsByDivision(String projectId, String division, int page, int size);
    List<BOQ> getBoqsByLevel(String projectId, int level);
    BOQ createSingleBoq(String projectId, BOQ boqData);
    BOQ updateSingleBoq(String projectId, String boqId, BOQ boqData);
}
