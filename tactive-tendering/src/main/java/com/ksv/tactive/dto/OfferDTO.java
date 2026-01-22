package com.ksv.tactive.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OfferDTO {
    private String id;
    private String tenderId;
    private String contractorId;
    private List<Map<String, Double>> boqRate;
    private List<String> paymentTerms;
    private String projectId;
}
