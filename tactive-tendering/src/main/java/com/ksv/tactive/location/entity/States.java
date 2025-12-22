package com.ksv.tactive.location.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class States {

    @Id
    private String id;
    private String state;
    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;
    private boolean active;
}
