package com.ksv.tactive.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ksv.tactive.dto.ContractorDto;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/tactive")
public class ContractorController {
	
	@PostMapping("/addContractor")
	public ResponseEntity<?> addContractor(@RequestBody ContractorDto contractorDto){
		return null;
	}

}
