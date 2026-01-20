package com.ksv.tactive.dto;

import com.ksv.tactive.project.entity.ScopeOfPackage;
import lombok.Data;

import java.sql.Date;
import java.util.Set;

@Data
public class TenderRespDto {
    private String id;
    private String tenderNumber;
    private String tenderName;
    private Date bidOpeningdate;
    private Date lastDate;
    private String floatedUser;
    private String contactName;
    private String contactNumber;
    private String contactEmail;
    private Set<BOQDto> boq;
    private Date floatedOn;
    private Set<ScopeOfPackage> scopeOfPackages;
    private boolean siteInvestigation;
    private boolean preBidding;
    private Date preBiddingDate;
    private Date siteInvestigationDate;
    private String offerSubmissionMode;
    private String siteInvestigationComments;
    private String preBiddingComments;
}
