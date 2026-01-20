package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.OfferQuotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferQuotationDao extends JpaRepository<OfferQuotation, String> {
}
