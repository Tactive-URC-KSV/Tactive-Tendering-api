package com.ksv.tactive.master.dao;

import com.ksv.tactive.master.entity.TerritoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TerritoryTypeDao extends JpaRepository<TerritoryType, String> {
    Optional<TerritoryType> findByTerritoryTypeIgnoreCaseAndActiveTrue(String territoryType);
}
