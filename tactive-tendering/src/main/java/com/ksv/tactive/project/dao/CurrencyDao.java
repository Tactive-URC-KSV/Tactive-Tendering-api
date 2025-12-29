package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ksv.tactive.project.entity.Currency;

import java.util.Optional;

public interface CurrencyDao extends JpaRepository<Currency, String>{

	Currency findByCurrencyCode(String currencyCode);
    Optional<Currency> findByCurrencyCodeIgnoreCaseAndActiveTrue(String currencyCode);
}
