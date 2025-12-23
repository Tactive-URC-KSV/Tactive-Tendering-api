package com.ksv.tactive.dto;

import com.ksv.tactive.project.entity.ActivityGroup;
import lombok.Data;

@Data
public class ActivityGroupDto {
    private String id;
    private String activityCode;
    private String activityName;
    private String costCodeTypeId;
    private boolean active;
}
