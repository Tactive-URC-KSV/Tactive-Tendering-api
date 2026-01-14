package com.ksv.tactive.project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Data
public class TenderInvites {

    @Id
    private String id;
    @ManyToOne
    @JoinColumn(name = "tender_id")
    private Tender tender;
    @ManyToOne
    @JoinColumn(name = "contractor_id")
    private Contractor contractor;
    private boolean submitted;
    @Column(name = "temp_token", unique = true)
    private String tempToken;
    private Timestamp tokenExpiry;
    private Date invitedOn;
}
