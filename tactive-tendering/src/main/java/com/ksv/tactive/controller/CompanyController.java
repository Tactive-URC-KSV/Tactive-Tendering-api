package com.ksv.tactive.controller;
import com.ksv.tactive.dto.CompanySaveDTO;
import com.ksv.tactive.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/tactive")
public class CompanyController {
	@Autowired
	private CompanyService companyService;
    @PostMapping("/company/add")
    public ResponseEntity<?> saveCompany(
            @RequestBody CompanySaveDTO companyDTO,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        companyService.saveCompany(companyDTO, files);
        return ResponseEntity.ok("Company saved successfully");
    }
}
