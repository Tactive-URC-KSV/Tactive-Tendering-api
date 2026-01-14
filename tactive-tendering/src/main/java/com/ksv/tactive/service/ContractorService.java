package com.ksv.tactive.service;

import com.ksv.tactive.dto.ContractorInputDto;
import com.ksv.tactive.dto.ContractorNatureDto;
import com.ksv.tactive.dto.ContractorResDto;
import com.ksv.tactive.dto.ContractorStatusDto;
import com.ksv.tactive.project.entity.*;
import com.ksv.tactive.util.ContractorStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public interface ContractorService {
    List<Map<String, Object>> getAllContractor();
    String addContractor(ContractorInputDto contractorInputDto, ContractorAddress contractorAddress,
                         ContractorContacts contractorContacts, ContractorTaxDetails contractorTaxDetails,
                         ContractorBankDetails contractorBankDetails, ContractorAddInfo contractorAddInfo);

    List<String> saveDocuments(List<MultipartFile> files);
    void addContractorType(ContractorType contractorType);
    void editContractorType(ContractorType contractorType);
    void addContractorNature(ContractorNatureDto contractorNatureDto);
    void addContractorGrade(ContractorGrade contractorGrade);
    void editContractorNature(ContractorNatureDto contractorNatureDto);
    void editContractorGrade(ContractorGrade contractorGrade);
    List<ContractorType> getAllContractorType();
    List<ContractorNature> getAllContractorNature();
    List<ContractorGrade> getAllContractorGrade();
    List<ContractorNature> getContractorNatureByContractorType(String contractorTypeId);
    List<ContractorStatusDto> getAllContractorStatus();
    String inviteContractor(ContractorFormInvite contractorFormInvite);
    List<ContractorFormInvite> getAllinvitedContractor();
    String generateToken(String id);
    ContractorResDto getContractorById(String contractorId);
    String verifyContractor(String contractorId, String contractorStatus);
    ContractorFormInvite getInviteById(String id);
    Contractor findContractorById(String contractorId);
    String generateTenderToken(String contractorId, String tenderId);
}
