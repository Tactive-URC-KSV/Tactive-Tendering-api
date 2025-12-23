package com.ksv.tactive.location.dao;
import com.ksv.tactive.location.entity.States;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface StatesDao extends JpaRepository<States, String> {

    List<States> findByCountryIdAndActiveTrue(String countryId);
    Optional<States> findByStateIgnoreCaseAndCountryIdAndActiveTrue(String state, String countryId);
}
