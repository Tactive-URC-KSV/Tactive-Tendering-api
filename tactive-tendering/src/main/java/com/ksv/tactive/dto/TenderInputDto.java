package com.ksv.tactive.dto;

import lombok.Data;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

@Data
public class TenderInputDto {
    private String id;
    private String tenderFloatingNo;
    private Date tenderFloatingDate;
    private String tenderName;
    private String contactName;
    private String contactNumber;
    private String contactEmail;
    private String floatedBy;
    private Date bidOpeningDate;
    private Date lastDate;
    private boolean siteInvestigation;
    private boolean preBidMeeting;
    private Date siteInvestigationFromDate;
    private Date siteInvestigationToDate;
    private Date preBidMeetingDate;
    private Time preBidMeetingTime;
    private Time siteInvestigationFromTime;
    private Time siteInvestigationToTime;
    private List<String> scopeId;
    private List<String> contractorId;
    private List<String> boqId;
    private String projectId;
    private String submissionMode;
}
