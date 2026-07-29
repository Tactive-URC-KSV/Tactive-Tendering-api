package com.ksv.tactive.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;
import com.ksv.tactive.dto.ExcelExtractBOQ;
import com.ksv.tactive.dto.PaginatedResponse;
import com.ksv.tactive.dto.ProjectInfoDto;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.project.entity.BOQ;
import com.ksv.tactive.project.entity.Template;
import com.ksv.tactive.service.BOQService;
import com.ksv.tactive.service.ProjectService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/tactive")
public class BOQController {

    @Autowired
    private BOQService boqService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private MessageSource messageSource;

    @PostMapping("/project/excel/getSheets")
    public ResponseEntity<?> getBOQFileData(@RequestParam("file") MultipartFile boqFile) {
        try {
            String fileName = boqFile.getOriginalFilename();
            if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                return ResponseEntity.status(HttpStatus.OK).body(boqService.getExcelSheets(boqFile));
            } else if (fileName.endsWith(".pdf")) {
                return ResponseEntity.status(HttpStatus.OK).body(boqService.getPdfPages(boqFile));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(messageSource.getMessage("file.type.not.supported", null, Locale.ENGLISH));
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/project/excel/getColumns/{sheetName}")
    public ResponseEntity<?> getColumns(@RequestParam("file") MultipartFile boqFile, @PathVariable String sheetName) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(boqService.getExcelColumns(boqFile, sheetName));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error reading the Excel file: " + e.getMessage());
        }
    }

    @PostMapping(value = "/project/extractedBOQ", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> getExtractedBOQData(@RequestParam("file") MultipartFile boqFile,
                                                 @RequestParam("sheetName") String sheetName,
                                                 @RequestPart("columnMapping") Map<String, List<String>> columnMapping, 
                                                 @RequestParam(required = false) String search,
                                                 @RequestParam(required = false) String pageBreakWord) {
        try {
            List<ExcelExtractBOQ> response = boqService.extractBOQData(sheetName,
                    columnMapping, boqFile, search, pageBreakWord);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing Excel file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: " + e.getMessage());
        }
    }

    @PutMapping("/update-boq-hierarchy/{projectId}")
    public ResponseEntity<?> updateBOQHierarchy(
            @PathVariable String projectId,
            @RequestBody Map<String, String> parentChildUpdateMap) {
        return ResponseEntity.status(HttpStatus.OK).body(boqService.updateBOQHierarchy(projectId, parentChildUpdateMap));
    }

    @PutMapping("/update-boq-division/{projectId}")
    public ResponseEntity<?> updateBOQDivision(
            @PathVariable String projectId,
            @RequestBody Map<String, String> boqDivisionUpdateMap) {
        return ResponseEntity.status(HttpStatus.OK).body(boqService.updateBOQDivision(projectId, boqDivisionUpdateMap));
    }

    @PostMapping(value = "project/mapBOQ/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> mapBoqData(@PathVariable String projectId, @RequestPart("file") MultipartFile boqFile,
                                        @RequestParam String sheetName,
                                        @RequestParam String columnMapping,
                                        @RequestParam String parentChildMapping,
                                        @RequestParam String lastLevelMapping,
                                        @RequestParam String levelMapping,
                                        @RequestParam(required = false) String ignoredSnos,
                                        @RequestParam(required = false) String pageBreakWord) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, List<String>> columnMap = mapper.readValue(columnMapping, new TypeReference<Map<String, List<String>>>() {});
            Map<Long, Long> parentChildMap = mapper.readValue(parentChildMapping, new TypeReference<Map<Long, Long>>() {});
            Map<Long, Boolean> lastLevelMap = mapper.readValue(lastLevelMapping, new TypeReference<Map<Long, Boolean>>() {});
            Map<Long, Long> levelMap = mapper.readValue(levelMapping, new TypeReference<Map<Long, Long>>() {});
            
            List<Long> ignoredSnoList = new ArrayList<>();
            if (ignoredSnos != null && !ignoredSnos.trim().isEmpty() && !ignoredSnos.equals("[]")) {
                ignoredSnoList = mapper.readValue(ignoredSnos, new TypeReference<List<Long>>() {});
            }

            return ResponseEntity.status(HttpStatus.OK).body(boqService.mapBOQData(
                    projectId, sheetName, columnMap, parentChildMap, lastLevelMap, levelMap, ignoredSnoList, boqFile, pageBreakWord
            ));
        } catch (IOException | DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing the Excel file: " + e.getMessage());
        }
    }

    @GetMapping("getParentBoq/{projectId}")
    public ResponseEntity<?> getParentBOQ(@PathVariable String projectId) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(boqService.getParentBOQ(projectId));
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
        }
    }

    @GetMapping("project/getParentBoq/{projectId}")
    public ResponseEntity<?> getParentBOQPaginated(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        try {
            Page<BOQ> boqPage = boqService.getParentBOQs(projectId, page, size);
            Map<String, Object> response = new HashMap<>();
            response.put("data", boqPage.getContent());
            response.put("currentPage", boqPage.getNumber());
            response.put("totalItems", boqPage.getTotalElements());
            response.put("totalPages", boqPage.getTotalPages());
            return ResponseEntity.ok(response);
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
        }
    }

    @GetMapping("project/getChildBoq/{projectId}/{parentBoqId}")
    public ResponseEntity<?> getChildBOQ(@PathVariable String projectId, @PathVariable String parentBoqId) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(boqService.getChildBOQ(projectId, parentBoqId));
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
        }
    }

    @GetMapping("project/getBoqTotalValue/{projectId}")
    public ResponseEntity<?> getBoqTotalValue(@PathVariable String projectId) {
        try {
            List<BOQ> boqList = boqService.getParentBOQ(projectId);
            double totalValue = 0;
            if (boqList != null) {
                totalValue = boqList.stream().mapToDouble(b -> b.getTotalAmount() != 0 ? b.getTotalAmount() : 0.0).sum();
            }
            return ResponseEntity.status(HttpStatus.OK).body(totalValue);
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("project/getParentBoqTotals/{projectId}")
    public ResponseEntity<?> getParentBoqTotals(@PathVariable String projectId) {
        try {
            return ResponseEntity.ok(boqService.getParentBoqTotalMap(projectId));
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("project/getBOQCount/{projectId}")
    public ResponseEntity<?> getBOQCount(@PathVariable String projectId) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(boqService.getBOQCount(projectId));
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("project/deleteBOQ/{projectId}")
    public ResponseEntity<?> deleteBOQ(@RequestBody List<String> boqCode, @PathVariable String projectId) {
        try {
            boqService.deleteBOQ(boqCode, projectId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(messageSource.getMessage("boq.delete.success", null, Locale.ENGLISH));
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/project/BOQ/{boqId}")
    public ResponseEntity<?> getBoqDetailById(@PathVariable String boqId) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(boqService.getBoqById(boqId));
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("project/Boq/excel/{projectId}")
    public ResponseEntity<?> generateBOQExcel(HttpServletResponse response, @PathVariable String projectId) {
        try {
            boqService.generateBoqExcel(projectId, response);
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (IOException | DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
        }
    }

    @GetMapping("project/Boq/pdf/{projectId}")
    public ResponseEntity<?> generateBOQPdf(HttpServletResponse response, @PathVariable String projectId) {
        try {
            boqService.generateBoqPdf(projectId, response);
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (IOException | DocumentException | DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
        }
    }

    @GetMapping("project/getAllBoqUploadedProject")
    public ResponseEntity<?> getAllBoqProjects() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(boqService.getAllBoqUploadedProject());
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
        }
    }

    @PostMapping("project/saveTemplateMapping")
    public ResponseEntity<?> saveTemplate(@RequestPart Template template, @RequestPart Map<String, List<String>> mapping) {
        return ResponseEntity.status(HttpStatus.OK).body(boqService.saveTemplate(template, mapping));
    }

    @GetMapping("project/getAllTemplate")
    public ResponseEntity<?> getAllTemplate() {
        List<Template> templates = boqService.getAllTemplate();
        if (templates.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(messageSource.getMessage("template.not.found", null, Locale.ENGLISH));
        }
        return ResponseEntity.status(HttpStatus.OK).body(templates);
    }

    @GetMapping("project/getTemplate/{templateId}")
    public ResponseEntity<?> getTemplateById(@PathVariable String templateId) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(boqService.getTemplateById(templateId));
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("project/getParentBoqAndAmount/{boqId}")
    public ResponseEntity<?> getParentBOQAndAmount(@PathVariable String boqId) {
        try {
            double parentBoqAmount = boqService.findTotalAmountForParentBoqs(boqId);
            return ResponseEntity.status(HttpStatus.OK).body(parentBoqAmount);
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
    @GetMapping("/searchBoq/{projectId}")
    public ResponseEntity<?> searchBoq(@PathVariable String projectId, @RequestParam String searchQuery) {
        try {
            return ResponseEntity.ok(boqService.searchBoq(projectId, searchQuery));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("project/getAllBoqDetails")
    public ResponseEntity<?> getAllBoqDetails(@RequestParam String projectId) {
        try {
            List<BOQ> boqList = boqService.getAllBOQsByProjectId(projectId);
            if (boqList == null || boqList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.status(HttpStatus.OK).body(boqList);
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
        }
    }

    @GetMapping("project/getDivisions/{projectId}")
    public ResponseEntity<?> getDivisions(@PathVariable String projectId) {
        try {
            List<String> divisions = boqService.getDivisionsByProjectId(projectId);
            return ResponseEntity.status(HttpStatus.OK).body(divisions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("project/getParentBoqByDivision/{projectId}/{division}")
    public ResponseEntity<?> getParentBoqByDivision(
            @PathVariable String projectId,
            @PathVariable String division,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        try {
            Page<BOQ> boqPage = boqService.getBOQsByDivision(projectId, division, page, size);
            Map<String, Object> response = new HashMap<>();
            response.put("data", boqPage.getContent());
            response.put("currentPage", boqPage.getNumber());
            response.put("totalItems", boqPage.getTotalElements());
            response.put("totalPages", boqPage.getTotalPages());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @GetMapping("project/getBoqsByLevel/{projectId}/{level}")
    public ResponseEntity<?> getBoqsByLevel(@PathVariable String projectId, @PathVariable int level) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(boqService.getBoqsByLevel(projectId, level));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("project/createSingleBoq/{projectId}")
    public ResponseEntity<?> createSingleBoq(@PathVariable String projectId, @RequestBody BOQ boqData) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(boqService.createSingleBoq(projectId, boqData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("project/updateSingleBoq/{projectId}/{boqId}")
    public ResponseEntity<?> updateSingleBoq(@PathVariable String projectId, @PathVariable String boqId, @RequestBody BOQ boqData) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(boqService.updateSingleBoq(projectId, boqId, boqData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
