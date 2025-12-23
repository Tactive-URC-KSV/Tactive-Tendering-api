package com.ksv.tactive.currency.dao;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ksv.tactive.currency.entity.Currency;

import java.util.Optional;

public interface CurrencyDao extends JpaRepository<Currency, String>{

	Currency findByCurrencyCode(String currencyCode);
    Optional<Currency> findByCurrencyCodeIgnoreCaseAndActiveTrue(String currencyCode);
}
