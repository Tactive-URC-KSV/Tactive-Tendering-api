package com.ksv.tactive.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ksv.tactive.project.entity.Currency;

public interface ProjectCurrencyDao extends JpaRepository<Currency, String>{

}
