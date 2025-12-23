package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class ContractorNatureDto {
    private String id;
    private String nature;
    private String entityTypeId;
    private boolean active;
}
