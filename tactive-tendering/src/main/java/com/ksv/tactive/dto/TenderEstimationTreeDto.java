package com.ksv.tactive.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TenderEstimationTreeDto {
    private String id;
    private String resourceType;
    private String resourceName;
    private String resourceNature;
    private double costUnitRate;
    private double totalCost;
    private String boqId;
    private List<TenderEstimationTreeDto> children = new ArrayList<>();
}