package com.ksv.tactive.master.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class TerritoryType {

    @Id
    private String id;
    private String territoryType;
    private boolean active;
}
