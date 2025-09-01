package com.ksv.tactive.dataloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksv.tactive.project.dao.ScopeOfPackDao;
import com.ksv.tactive.project.entity.ScopeOfPackage;

import jakarta.transaction.Transactional;

@Component
public class ScopeLoader implements MasterDataLoader<ScopeOfPackage> {

	private static final Logger logger = LoggerFactory.getLogger(ActivityGroupLoader.class);

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ScopeOfPackDao scopeOfPackDao;

	@Transactional
	@Override
	public void loadData() throws IOException, DataAccessException {
		logger.info("Starting data load for Scope of Packages");
		try (InputStream inputStream = getClass().getResourceAsStream("/data/scope_data.json")) {
			if (inputStream == null) {
				throw new IOException("Resource file 'standard-costcodes.json' not found");
			}
			List<ScopeOfPackage>  scope= objectMapper.readValue(inputStream, new TypeReference<>() {});
			if (scopeOfPackDao.count() == 0) {
				for (ScopeOfPackage sc : scope) {
					sc.setId(UUID.randomUUID().toString());
					scopeOfPackDao.save(sc);
				}
				logger.info("Loaded {} Scope of Package records", scope.size());
			} else {
				logger.info("Skipping data load; Scope of Package table is not empty");
			}
			
		}
	}

}
