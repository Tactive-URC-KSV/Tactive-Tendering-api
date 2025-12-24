package com.ksv.tactive.controller;

import com.ksv.tactive.contractor.entity.*;
import com.ksv.tactive.dto.ContractorInputDto;
import com.ksv.tactive.dto.ContractorNatureDto;
import com.ksv.tactive.service.ContractorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/tactive")
public class ContractorController {

	@Autowired
	private ContractorService contractorService;
	@Autowired
	private MessageSource messageSource;

	@PostMapping("/contractorType/add")
	public ResponseEntity<?> addContractorType(@RequestBody ContractorType contractorType){
		contractorService.addContractorType(contractorType);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.save.success",null, Locale.ENGLISH));
	}
	@PutMapping("/contractorType/edit")
	public ResponseEntity<?> editContractorType(@RequestBody ContractorType contractorType){
		contractorService.editContractorType(contractorType);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated",null, Locale.ENGLISH));
	}
	@GetMapping("/contractorType")
	public ResponseEntity<?> getAllContractorType(){
		return ResponseEntity.status(HttpStatus.OK).body(contractorService.getAllContractorType());
	}
	@PostMapping("/contractorNature/add")
	public ResponseEntity<?> addContractorNature(@RequestBody ContractorNatureDto contractorNature){
		contractorService.addContractorNature(contractorNature);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.save.success",null, Locale.ENGLISH));
	}
	@PutMapping("/contractorNature/edit")
	public ResponseEntity<?> editContractorNature(@RequestBody ContractorNatureDto contractorNature){
		contractorService.editContractorNature(contractorNature);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated",null, Locale.ENGLISH));
	}
	@GetMapping("/contractorNature")
	public ResponseEntity<?> getAllContractorNature(){
		return ResponseEntity.status(HttpStatus.OK).body(contractorService.getAllContractorNature());
	}
	@PostMapping("/contractorGrade/add")
	public ResponseEntity<?> addContractorGrade(@RequestBody ContractorGrade contractorGrade){
		contractorService.addContractorGrade(contractorGrade);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.save.success",null, Locale.ENGLISH));
	}
	@PutMapping("/contractorGrade/edit")
	public ResponseEntity<?> editContractorGrade(@RequestBody ContractorGrade contractorGrade){
		contractorService.editContractorGrade(contractorGrade);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated",null, Locale.ENGLISH));
	}
	@GetMapping("/contractorGrade")
	public ResponseEntity<?> getAllContractorGrade(){
		return ResponseEntity.status(HttpStatus.OK).body(contractorService.getAllContractorGrade());
	}
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
