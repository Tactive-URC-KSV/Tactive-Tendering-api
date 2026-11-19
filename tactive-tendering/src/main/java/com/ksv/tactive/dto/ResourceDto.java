package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class ResourceDto {
    private String id;
    private String resourceName;
    private String resourceCode;
    private String resourceTypeId;
    private String uomId;
    private int level;
    private boolean lastLevel;
    private boolean active;
    private String parentId;
}
