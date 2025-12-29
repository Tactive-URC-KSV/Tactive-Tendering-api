package com.ksv.tactive.service;

import com.ksv.tactive.dto.ContractorInputDto;
import com.ksv.tactive.dto.ContractorNatureDto;
import com.ksv.tactive.project.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface ContractorService {
    List<Contractor> getAllContractor();
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
}
