package com.ksv.tactive.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
public class TenderEditHistory {
    @Id
    private String id;
    private String field;
    private String oldValue;
    private String newValue;
    @ManyToOne
    @JoinColumn(name = "tender_id")
    private Tender tender;
    private String remarks;
    private String updatedBy;
    private String updatedUserId;
    private Timestamp createdAt;
}
