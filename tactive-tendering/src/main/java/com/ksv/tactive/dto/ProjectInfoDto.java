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
    private Double totalEstimatedValue;
    private String status;
    private String regionName;
    private Date createdAt;
    private boolean archieve;
    private String companyId;
    private boolean needFeasibility;
    
}
