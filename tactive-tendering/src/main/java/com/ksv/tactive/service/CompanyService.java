package com.ksv.tactive.service;
import org.springframework.stereotype.Service;
import com.ksv.tactive.company.entity.Company;
@Service
public interface CompanyService {
	String addCompany(Company company);
}
