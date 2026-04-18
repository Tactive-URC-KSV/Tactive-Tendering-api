package com.ksv.tactive.dto;

import lombok.Data;

import java.sql.Date;
import java.util.List;
import java.util.Set;

@Data
public class ContractorDTO {

    private String id;
    private String entityCode;
    private String entityName;
    private String inviteId;
    private Date effectiveDate;
    private String contractorTypeId;
    private String contractorGradeId;
    private Set<String> contractorNatureIds;
    private String submissionMode;
    private String status;
    private List<String> attachmentUrls;
    private List<ContactDTO> contacts;
    private List<AddressDTO> addresses;
    private List<TaxDTO> taxDetails;
    private List<BankDTO> bankDetails;
    private List<AdditionalInfoDTO> additionalInfo;
}