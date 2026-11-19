package com.ksv.tactive.project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Entity
public class TenderEditHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;
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
