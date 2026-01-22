package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.TenderEditHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenderEditHistoryDao extends JpaRepository<TenderEditHistory, String> {
    List<TenderEditHistory> findByTenderId(String tenderId);
}
