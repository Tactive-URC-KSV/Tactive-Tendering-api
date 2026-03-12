package com.ksv.tactive.project.entity;

import com.ksv.tactive.util.ContractorStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.util.UUID;

@Entity
@Data
public class ContractorFormInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;
    private String email;
    private String name;
    private String message;
    private boolean submitted;
    private String invitedBy;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ContractorStatus contractorStatus;
    private Date invitedOn;
    private String username;
}
