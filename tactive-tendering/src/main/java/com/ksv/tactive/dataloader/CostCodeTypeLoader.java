package com.ksv.tactive.dataloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksv.tactive.project.dao.CostCodeTypeDao;
import com.ksv.tactive.project.entity.CostCodeType;

import jakarta.transaction.Transactional;

@Component
public class CostCodeTypeLoader implements MasterDataLoader<CostCodeType>{
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private CostCodeTypeDao codeTypeDao;

	@Transactional
	@Override
	public void loadData() throws IOException, DataAccessException {
		try(InputStream inputStream = getClass().getResourceAsStream("/data/cost-code-types.json")){
			if(inputStream == null) {
				throw new IOException("Resource file 'cost-code-types.json' not found");
			}
			List<CostCodeType> costCodeTypes = objectMapper.readValue(inputStream, new TypeReference<>() {});
			if(codeTypeDao.count() == 0) {
				for(CostCodeType costCodeType : costCodeTypes) {
					costCodeType.setId(UUID.randomUUID().toString());
					codeTypeDao.save(costCodeType);
				}
			}
		}
	}

}
