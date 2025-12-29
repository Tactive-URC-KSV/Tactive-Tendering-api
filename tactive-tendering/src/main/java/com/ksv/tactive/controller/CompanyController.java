package com.ksv.tactive.controller;
import com.ksv.tactive.dto.CompanySaveDTO;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/tactive")
public class CompanyController {
	@Autowired
	private CompanyService companyService;
    @PostMapping( value = "/company/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveCompany(
            @RequestPart("companyDTO") CompanySaveDTO companyDTO,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        try{
            companyService.saveCompany(companyDTO, files);
            return ResponseEntity.ok("Company saved successfully");
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
