package com.ksv.tactive.controller;

import com.ksv.tactive.contractor.entity.*;
import com.ksv.tactive.dto.ContractorInputDto;
import com.ksv.tactive.service.ContractorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/tactive")
public class ContractorController {

	@Autowired
	private ContractorService contractorService;
	
	@PostMapping("/addContractor")
	public ResponseEntity<?> addContractor(@RequestBody ContractorInputDto contractorInputDto,
										   @RequestBody ContractorAddress contractorAddress,
										   @RequestBody ContractorContacts contractorContacts,
										   @RequestBody ContractorTaxDetails contractorTaxDetails,
										   @RequestBody ContractorBankDetails contractorBankDetails,
										   @RequestBody ContractorAddInfo contractorAddInfo,
										   @RequestParam List<MultipartFile> files){
		List<String> paths = contractorService.saveDocuments(files);
		contractorInputDto.setPaths(paths);
		String message = contractorService.addContractor(contractorInputDto,contractorAddress,contractorContacts,
				contractorTaxDetails,contractorBankDetails,contractorAddInfo);
		return ResponseEntity.status(HttpStatus.OK).body(message);
	}

}
