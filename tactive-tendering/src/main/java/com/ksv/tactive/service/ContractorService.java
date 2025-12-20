package com.ksv.tactive.service;

import com.ksv.tactive.contractor.entity.*;
import com.ksv.tactive.dto.ContractorInputDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface ContractorService {
    List<Contractor> getAllContractor();
    String addContractor(ContractorInputDto contractorInputDto, ContractorAddress contractorAddress,
                         ContractorContacts contractorContacts,ContractorTaxDetails contractorTaxDetails,
                         ContractorBankDetails contractorBankDetails,ContractorAddInfo contractorAddInfo);

    List<String> saveDocuments(List<MultipartFile> files);
}
