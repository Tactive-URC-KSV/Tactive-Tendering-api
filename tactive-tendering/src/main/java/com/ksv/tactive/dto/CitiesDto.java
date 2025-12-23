package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class CitiesDto {
    private String id;
    private String city;
    private String stateId;
    private String countryId;
    private boolean active;
}
