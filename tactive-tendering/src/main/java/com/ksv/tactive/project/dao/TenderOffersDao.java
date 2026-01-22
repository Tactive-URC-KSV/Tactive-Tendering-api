package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.TenderOffers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenderOffersDao extends JpaRepository<TenderOffers, String> {
    long countByTenderIdAndContractorId(String tenderId, String contractorId);
    List<TenderOffers> findByTenderId(String tenderId);
}
