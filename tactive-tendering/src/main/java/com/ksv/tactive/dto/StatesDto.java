package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class StatesDto {
    private String id;
    private String state;
    private String countryId;
    private boolean active;
}
