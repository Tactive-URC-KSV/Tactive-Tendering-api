package com.ksv.tactive.master.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class IdentityType {

    @Id
    private String id;
    private String idType;
    private boolean active;
}
