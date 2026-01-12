package com.ksv.tactive.controller;

import com.ksv.tactive.dto.ContractorInputDto;
import com.ksv.tactive.dto.ContractorNatureDto;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.exception.FormAlreadySubmittedException;
import com.ksv.tactive.project.entity.*;
import com.ksv.tactive.service.ContractorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

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
	@GetMapping("/contractorNature/{contractorTypeId}")
	public ResponseEntity<?> getContractorNatureByContractorType(@PathVariable String contractorTypeId){
		return ResponseEntity.status(HttpStatus.OK).body(contractorService.getContractorNatureByContractorType(contractorTypeId));
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
	@PostMapping(value = "/addContractor", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> addContractor(@RequestPart ContractorInputDto contractorInputDto,
										   @RequestPart ContractorAddress contractorAddress,
										   @RequestPart ContractorContacts contractorContacts,
										   @RequestPart ContractorTaxDetails contractorTaxDetails,
										   @RequestPart ContractorBankDetails contractorBankDetails,
										   @RequestPart ContractorAddInfo contractorAddInfo,
										   @RequestParam(required = false, value = "files") List<MultipartFile> files){
		List<String> paths = contractorService.saveDocuments(files);
		contractorInputDto.setPaths(paths);
		String message = contractorService.addContractor(contractorInputDto,contractorAddress,contractorContacts,
				contractorTaxDetails,contractorBankDetails,contractorAddInfo);
		return ResponseEntity.status(HttpStatus.OK).body(message);
	}
	@GetMapping("/contractor")
	public ResponseEntity<?> getAllContractor(){
		return ResponseEntity.status(HttpStatus.OK).body(contractorService.getAllContractor());
	}
	@GetMapping("/contractorStatus")
	public ResponseEntity<?> getAllContractorStatus(){
		return ResponseEntity.status(HttpStatus.OK).body(contractorService.getAllContractorStatus());
	}
	@PostMapping("/contractor/invite")
	public ResponseEntity<?> inviteContractor(@RequestBody ContractorFormInvite contractorFormInvite){
		try {
			return ResponseEntity.status(HttpStatus.OK).body(contractorService.inviteContractor(contractorFormInvite));
		}
		catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}
	@GetMapping("/invitedContractor")
	public ResponseEntity<?> getAllInvitedContractor(){
		return ResponseEntity.status(HttpStatus.OK).body(contractorService.getAllinvitedContractor());
	}
	@GetMapping("/validateInvite")
	public ResponseEntity<?> validateContractorInvite(@RequestParam String id) {
		try {
			ContractorFormInvite invite = contractorService.getInviteById(id);
			String token = contractorService.generateToken(id);
			Map<String, Object> response = new HashMap<>();
			response.put("contractor", invite);
			response.put("token", token);
			return ResponseEntity.ok(response);
		} catch (FormAlreadySubmittedException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Collections.singletonMap("message", e.getMessage()));
		} catch (DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}
	@GetMapping("/contractor/{contractorId}")
	public ResponseEntity<?> getContractorById(@PathVariable String contractorId){
		try{
			return ResponseEntity.status(HttpStatus.OK).body(contractorService.getContractorById(contractorId));
		}
		catch (DataNotFoundException e){
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}
	@PutMapping("/contractor/verify/{contractorId}")
	public ResponseEntity<?> verifyContractor(@PathVariable String contractorId,
											  @RequestParam String contractorStatus){
		return ResponseEntity.status(HttpStatus.OK).body(contractorService.verifyContractor(contractorId, contractorStatus));
	}
}
