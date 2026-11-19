package com.ksv.tactive.project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"base_currency_id", "conversion_currency_id", "effective_date"}
        )
)
public class CurrencyConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "base_currency_id", nullable = false)
    private Currency baseCurrency;
    @ManyToOne
    @JoinColumn(name = "conversion_currency_id", nullable = false)
    private Currency conversionCurrency;
    @Column(precision = 19, scale = 6, nullable = false)
    private BigDecimal conversionRate;
    private LocalDate effectiveDate;
}
