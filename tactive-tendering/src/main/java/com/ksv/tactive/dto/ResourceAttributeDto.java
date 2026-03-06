package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class ResourceAttributeDto {
    private String id;
    private String resourceId;
    private String attributeGroupId;
    private boolean mandatory;
}
