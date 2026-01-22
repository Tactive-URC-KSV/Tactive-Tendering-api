package com.ksv.tactive.dto;

import lombok.Data;

@Data
public class NegotiateDTO {
    private String offerId;
    private String contractorId;
    private String tenderId;
    private int offerVersion;
    private String comments;
}
