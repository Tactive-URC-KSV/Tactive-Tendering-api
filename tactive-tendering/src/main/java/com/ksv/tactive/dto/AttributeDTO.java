package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class AttributeDTO {
    private String id;
    private long attributeCode;
    private String attributeName;
    private boolean active;
}
