package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class ResourceDto {
    private String id;
    private String resourceName;
    private String resourceCode;
    private String resourceTypeId;
    private double unitRate;
    private String uomId;
    private boolean active;
}
