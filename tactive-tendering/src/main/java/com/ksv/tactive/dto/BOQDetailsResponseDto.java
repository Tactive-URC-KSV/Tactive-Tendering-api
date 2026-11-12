package com.ksv.tactive.dto;

import com.ksv.tactive.project.entity.BOQ;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
public class BOQDetailsResponseDto {
    private UUID id;
    private String boqCode;
    private String boqName;
    private int level;
    private double quantity;
    private double totalRate;
    private double totalAmount;
    private boolean lastLevel;
    private String uomCode;
    private String clientUom;
    private String projectId;
    private Integer childCount;
    private String parentBoqId;
    private String division;
    private Long pageNo;
    private Double enqSlNo;
    private Integer version;
    private Long excelSno;
    private String boqShortName;
    private String description;
    private boolean priceBoq;
    private boolean archive;
    private Timestamp createdAt;

    public static BOQDetailsResponseDto from(BOQ boq) {
        BOQDetailsResponseDto dto = new BOQDetailsResponseDto();
        dto.setId(boq.getId());
        dto.setBoqCode(boq.getBoqCode());
        dto.setBoqName(boq.getBoqName());
        dto.setLevel(boq.getLevel());
        dto.setQuantity(boq.getQuantity());
        dto.setTotalRate(boq.getTotalRate());
        dto.setTotalAmount(boq.getTotalAmount());
        dto.setLastLevel(boq.isLastLevel());
        dto.setUomCode(boq.getUom() != null ? boq.getUom().getUomCode() : null);
        dto.setClientUom(boq.getUomCode());       
        dto.setProjectId(boq.getProjectId());
        dto.setChildCount(boq.getChildCount());
        dto.setParentBoqId(boq.getParentBoqId());
        dto.setDivision(boq.getDivision());
        dto.setPageNo(boq.getPageNo());
        dto.setEnqSlNo(boq.getEnqSlNo());
        dto.setVersion(boq.getVersion());
        dto.setExcelSno(boq.getExcelSno());
        dto.setBoqShortName(boq.getBoqShortName());
        dto.setDescription(boq.getDescription());
        dto.setPriceBoq(boq.isPriceBoq());
        dto.setArchive(boq.isArchive());
        dto.setCreatedAt(boq.getCreatedAt());
        return dto;
    }
}
