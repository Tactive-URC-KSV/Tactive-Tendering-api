package com.ksv.tactive.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ksv.tactive.util.FormulaElementType;
import com.ksv.tactive.util.FormulaSourceType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(indexes = {
        @Index(name = "idx_formula_tender", columnList = "tender_estimation_id"),
        @Index(name = "idx_formula_sequence", columnList = "sequence")
})
public class TenderFormulaElement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "tender_estimation_id")
    private TenderEstimation tenderEstimation;

    private int sequence;

    @Enumerated(EnumType.STRING)
    private FormulaElementType type;

    @Enumerated(EnumType.STRING)
    private FormulaSourceType sourceType;

    @ManyToOne
    @JoinColumn(name = "boq_id")
    private BOQ boq;

    @ManyToOne
    @JoinColumn(name = "resource_id")
    private TenderEstimation resource;

    @ManyToOne
    @JoinColumn(name = "global_id")
    private GlobalValue globalValue;

    private Double value;

    private String operator;
}