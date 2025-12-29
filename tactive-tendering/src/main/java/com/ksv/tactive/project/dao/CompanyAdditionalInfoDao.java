package com.ksv.tactive.project.dao;
import com.ksv.tactive.project.entity.CompanyAdditionalInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface CompanyAdditionalInfoDao extends JpaRepository<CompanyAdditionalInfo, String>{
}
