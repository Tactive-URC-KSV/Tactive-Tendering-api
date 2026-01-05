package com.ksv.tactive.dto;

import com.ksv.tactive.project.entity.*;
import lombok.Data;

@Data
public class ContractorResDto {
    private Contractor contractor;
    private ContractorContacts contractorContacts;
    private ContractorBankDetails contractorBankDetails;
    private ContractorTaxDetails contractorTaxDetails;
    private ContractorAddInfo contractorAddInfo;
    private ContractorAddress contractorAddress;
}
