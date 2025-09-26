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
import com.ksv.tactive.project.dao.UOMDao;
import com.ksv.tactive.project.entity.UOM;

import jakarta.transaction.Transactional;

@Component
public class UomLoader implements MasterDataLoader<UOM> {
	private static final Logger logger = LoggerFactory.getLogger(UomLoader.class);

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UOMDao uomDao;

	@Transactional
	@Override
	public void loadData() throws IOException, DataAccessException {
		logger.info("Starting data load for UOM");
		try (InputStream inputStream = getClass().getResourceAsStream("/data/uom.json")) {
			if (inputStream == null) {
				throw new IOException("Resource file 'standard-costcodes.json' not found");
			}
			List<UOM> uoms = objectMapper.readValue(inputStream, new TypeReference<>() {
			});

			for (UOM uom : uoms) {
				if (uomDao.findByUomCodeIgnoreCase(uom.getUomCode()) != null) {
					continue;
				}
				uom.setId(UUID.randomUUID().toString());
				uomDao.save(uom);
			}

		}

	}

}
