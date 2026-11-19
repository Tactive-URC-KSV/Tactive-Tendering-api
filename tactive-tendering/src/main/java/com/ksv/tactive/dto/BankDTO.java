package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class BankDTO {

    private String id;

    private String accHolderName;
    private String accNumber;
    private String bankName;
    private String branch;
    private String bankAddress;
}