package com.ksv.tactive.dto;

import lombok.Data;

import java.util.List;

@Data
public class AttributeGroupDTO {
    private String id;
    private long groupCode;
    private String groupName;
    private boolean active;
}
