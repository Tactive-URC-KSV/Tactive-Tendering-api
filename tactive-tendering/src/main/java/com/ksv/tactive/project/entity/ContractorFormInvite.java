package com.ksv.tactive.project.entity;

import com.ksv.tactive.util.ContractorStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;

@Entity
@Data
public class ContractorFormInvite {

    @Id
    private String id;
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
