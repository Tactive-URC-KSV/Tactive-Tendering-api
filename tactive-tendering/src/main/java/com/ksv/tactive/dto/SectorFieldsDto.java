package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class SectorFieldsDto {
    private String id;
    private String fieldName;
    private String fieldType;
    private boolean mandatory;
    private String sectorId;
    private String fieldSection;
    private boolean active;
}
