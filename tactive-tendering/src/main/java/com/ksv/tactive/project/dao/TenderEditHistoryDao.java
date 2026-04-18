package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.TenderEditHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TenderEditHistoryDao extends JpaRepository<TenderEditHistory, UUID> {
    List<TenderEditHistory> findByTenderId(UUID tenderId);
}
