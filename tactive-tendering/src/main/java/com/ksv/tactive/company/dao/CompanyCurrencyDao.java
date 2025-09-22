package com.ksv.tactive.company.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ksv.tactive.company.entity.Currency;

public interface CompanyCurrencyDao extends JpaRepository<Currency, String>{

}
