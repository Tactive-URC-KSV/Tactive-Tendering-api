package com.ksv.tactive.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class States {

    private String id;
    private String state;
    private Country country;
    private boolean active;
}
