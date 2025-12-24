package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private String id;
    private String address1;
    private String address2;
    private String zipcode;
    private String email;
    private String website;
    private String faxNo;
    private String phone;
    private String addressTypeId;
}
