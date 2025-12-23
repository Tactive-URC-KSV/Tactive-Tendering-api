package com.ksv.tactive.location.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.location.entity.Cities;

@Repository
public interface CitiesDao extends JpaRepository<Cities, String>{

	Cities findByCityAndCountryId(String city, String countryId);
	List<Cities> findByCountryId(String countryId);
    List<Cities> findByStatesIdAndActiveTrue(String stateId);
	Optional<Cities> findByCityIgnoreCaseAndStatesIdAndActiveTrue(String city, String stateId);
	Optional<Cities> findByCityIgnoreCaseAndStates_IdAndActiveTrue(String city, String stateId);
}
