package com.ksv.tactive.dto;

import java.sql.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectInfoDto {
    private String projectId;
    private String projectCode;
    private String projectName;
    private Date startDate;
    private Date endDate;
    private String sectorName;
    private Double estimatedValue;
    private String status;
    private String regionName;

    
    public ProjectInfoDto(String projectId, String projectCode, String projectName,
            Date startDate, Date endDate, String sectorName,
            String regionName, Double estimatedValue, String status) {
    	this.projectId = projectId;
    	this.projectCode = projectCode;
    	this.projectName = projectName;
    	this.startDate = startDate;
    	this.endDate = endDate;
    	this.sectorName = sectorName;
    	this.regionName = regionName;
    	this.estimatedValue = estimatedValue;
    	this.status = status;
    }

}
