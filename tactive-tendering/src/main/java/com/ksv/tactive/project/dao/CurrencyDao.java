package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ksv.tactive.project.entity.Currency;

import java.util.Optional;
import java.util.UUID;

public interface CurrencyDao extends JpaRepository<Currency, UUID>{

	Currency findByCurrencyCode(String currencyCode);
    Optional<Currency> findByCurrencyCodeIgnoreCaseAndActiveTrue(String currencyCode);
}
