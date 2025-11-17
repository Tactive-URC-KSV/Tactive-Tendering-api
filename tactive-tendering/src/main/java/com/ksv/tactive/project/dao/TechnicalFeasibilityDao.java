package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ksv.tactive.project.entity.TechnicalFeasibility;
public interface TechnicalFeasibilityDao extends JpaRepository<TechnicalFeasibility, String>{
}
