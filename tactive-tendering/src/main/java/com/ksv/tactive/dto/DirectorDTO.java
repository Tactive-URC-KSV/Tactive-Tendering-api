package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class DirectorDTO {

    private String id;
    private String directorTypeId;
    private String directorName;
    private Double sharePercentage;
    private Integer noOfShares;
}
