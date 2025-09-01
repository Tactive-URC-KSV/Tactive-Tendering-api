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
import com.ksv.tactive.project.dao.RegionDao;
import com.ksv.tactive.project.entity.Region;

import jakarta.transaction.Transactional;

@Component
public class RegionLoader implements MasterDataLoader<Region>{

	private static final Logger logger = LoggerFactory.getLogger(ActivityGroupLoader.class);

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private RegionDao regionDao;
	
	@Transactional
	@Override
	public void loadData() throws IOException, DataAccessException {
		logger.info("Starting data load for Region");
		try (InputStream inputStream = getClass().getResourceAsStream("/data/regions.json")) {
			if (inputStream == null) {
				throw new IOException("Resource file 'regions.json' not found");
			}
			List<Region> regions = objectMapper.readValue(inputStream, new TypeReference<>() {});
			if (regionDao.count() == 0) {
				for (Region region : regions) {
					region.setId(UUID.randomUUID().toString());
					regionDao.save(region);
				}
				logger.info("Loaded {} Region records", regions.size());
			} else {
				logger.info("Skipping data load; Region table is not empty");
			}
		}
		
	}

}
