package com.ksv.tactive.dto;

import lombok.Data;

import java.util.List;

@Data
public class CompanySaveDTO {

    /* ===== BASIC INFO ===== */
    private String companyId;          // null for create
    private String companyName;
    private String shortName;
    private String parentCompanyId;

    private String comTypeId;
    private String comLevelId;
    private String comNatureId;
    private String businessNatureId;
    private String companyConstitutionId;
    private String statusId;

    private String languageId;
    private String currencyId;
    private String bank;

    /* ===== ONE–TO–ONE ===== */
    private AddressDTO address;

    private CompanyProfileDTO profile;

    /* ===== ONE–TO–MANY ===== */
    private List<ContactDTO> contacts;
    private List<TaxDTO> taxDetails;
    private List<DirectorDTO> directors;
    private List<JointVentureDTO> jointVentures;
    private List<AdditionalInfoDTO> additionalInfos;
    private List<LocalNameDTO> localNames;
}

