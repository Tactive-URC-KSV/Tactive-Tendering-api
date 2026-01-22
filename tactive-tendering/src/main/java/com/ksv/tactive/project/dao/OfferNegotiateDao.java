package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.OfferNegotiation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferNegotiateDao extends JpaRepository<OfferNegotiation, String> {
}
