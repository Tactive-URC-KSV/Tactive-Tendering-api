package com.ksv.tactive.company.dao;
import com.ksv.tactive.company.entity.CompanyAdditionalInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface CompanyAdditionalInfoDao extends JpaRepository<CompanyAdditionalInfo, String>{
}
