package com.ksv.tactive.dto;

import lombok.Data;

import java.sql.Date;
import java.util.List;
import java.util.Set;

@Data
public class ContractorInputDto {
    private String entityCode;
    private String entityName;
    private Date effectiveDate;
    private String contractorTypeId;
    private String contractorGradeId;
    private List<String> paths;
    private Set<String> contractorNatureId;
}
