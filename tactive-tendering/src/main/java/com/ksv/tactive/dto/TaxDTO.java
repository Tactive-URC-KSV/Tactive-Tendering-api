package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class TaxDTO {
    private String id;
    private String effectiveFrom;
    private String effectiveTo;

    private String taxTypeId;
    private String territoryTypeId;
    private String territory;
    private String taxRegNo;
    private String taxRegDate;

    private String address1;
    private String address2;
    private String city;
    private String pinCode;
    private String email;
}

