package com.ksv.tactive.dto;

import lombok.Data;

import java.util.List;

@Data
public class InternalBoqProjectDto {

    private String internalBoqId;
    private String resourceId;
    private String resourceName;
    private String uomCode;
    private double totalQuantity;

    private List<InternalBoqItemDto> items;
}
