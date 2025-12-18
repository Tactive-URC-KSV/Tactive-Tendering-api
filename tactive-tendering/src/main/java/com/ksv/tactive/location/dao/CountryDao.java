package com.ksv.tactive.location.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.location.entity.Country;

@Repository
public interface CountryDao extends JpaRepository<Country, String>{

	Country findByCountry(String country);

}
