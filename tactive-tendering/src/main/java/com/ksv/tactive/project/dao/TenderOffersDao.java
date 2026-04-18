package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.TenderOffers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenderOffersDao extends JpaRepository<TenderOffers, UUID> {
    List<TenderOffers> findByTenderId(UUID tenderId);
    Optional<TenderOffers> findByTenderIdAndContractorId(UUID tenderId, UUID contractorId);
}
