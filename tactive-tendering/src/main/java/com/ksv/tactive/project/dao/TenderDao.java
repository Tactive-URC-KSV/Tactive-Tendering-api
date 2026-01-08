package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.Tender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenderDao extends JpaRepository<Tender, String> {
    List<Tender> findByProjectsId(String projectId);
}
