package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.Currency;
import com.ksv.tactive.project.entity.CurrencyConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CurrencyConversionDao extends JpaRepository<CurrencyConversion, UUID> {
    Optional<CurrencyConversion> findByBaseCurrencyAndConversionCurrencyAndEffectiveDate(Currency base, Currency target, LocalDate effectiveDate);
    List<CurrencyConversion> findLatestByBaseCurrency(Currency base);
}
