package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.OfferQuotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OfferQuotationDao extends JpaRepository<OfferQuotation, UUID> {
    List<OfferQuotation> findByTenderOffersId(UUID id);
}
