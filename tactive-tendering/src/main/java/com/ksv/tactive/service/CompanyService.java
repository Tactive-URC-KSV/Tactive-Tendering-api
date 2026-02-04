package com.ksv.tactive.service;
import com.ksv.tactive.dto.CompanySaveDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public interface CompanyService {
	void saveCompany(CompanySaveDTO dto, List<MultipartFile> files);
    List<Map<String, String>> getAllCompany();
}
