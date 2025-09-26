package com.ksv.tactive.dataloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksv.tactive.project.dao.SectorDao;
import com.ksv.tactive.project.entity.Sector;

import jakarta.transaction.Transactional;

@Component
public class SectorLoader implements MasterDataLoader<Sector>{
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SectorLoader.class);

	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private SectorDao sectorDao;
	
	
	@Transactional
	@Override
	public void loadData() throws IOException, DataAccessException {
		logger.info("Starting data load for Sector");
		try(InputStream inputStream = getClass().getResourceAsStream("/data/sectors.json")){
			if (inputStream == null) {
				throw new IOException("Resource file 'sectors.json' not found");
			}
			List<Sector> sectors = objectMapper.readValue(inputStream, new TypeReference<>() {});
			
				for (Sector sector : sectors) {
					if(sectorDao.findBySectorNameIgnoreCase(sector.getSectorName()) != null) {
						continue;
					}
					sector.setId(UUID.randomUUID().toString());
					sectorDao.save(sector);
				}
				
		}
		
	}

}
