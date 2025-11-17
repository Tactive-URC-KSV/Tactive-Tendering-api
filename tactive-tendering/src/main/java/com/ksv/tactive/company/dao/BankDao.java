package com.ksv.tactive.company.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.company.entity.Bank;
@Repository
public interface BankDao extends JpaRepository<Bank, String>{
}
