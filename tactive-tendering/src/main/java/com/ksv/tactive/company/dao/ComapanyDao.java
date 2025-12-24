package com.ksv.tactive.company.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ksv.tactive.company.entity.Company;
import org.springframework.stereotype.Repository;

@Repository
public interface ComapanyDao extends JpaRepository<Company, String>{
}
