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
import com.ksv.tactive.master.dao.QuantityTypeDao;
import com.ksv.tactive.master.dao.ResourceDao;
import com.ksv.tactive.master.dao.ResourceTypeDao;
import com.ksv.tactive.master.dao.UOMDao;
import com.ksv.tactive.master.entity.QuantityType;
import com.ksv.tactive.master.entity.ResourceType;
import com.ksv.tactive.master.entity.Resources;
import com.ksv.tactive.master.entity.UOM;

import jakarta.transaction.Transactional;
@Component
public class ResourceLoader implements MasterDataLoader<Resources> {
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ResourceTypeDao resourceTypeDao;
	@Autowired
	private UOMDao uomDao;
	@Autowired
	private QuantityTypeDao quantityTypeDao;
	@Autowired
	private ResourceDao resourceDao;
	@Transactional
	@Override
	public void loadData() throws IOException, DataAccessException {
		try (InputStream inputStream = getClass().getResourceAsStream("/data/resources.json")) {
			if (inputStream == null) {
				throw new IOException("Resource file 'standard-costcodes.json' not found");
			}
			List<Resources> resources = objectMapper.readValue(inputStream, new TypeReference<>() {});
			for(Resources res : resources) {
				List<Resources> existing = resourceDao.findByResourceNameIgnoreCase(res.getResourceName());
				if(existing != null && !existing.isEmpty()) {
					boolean exists = false;
					for(Resources ex : existing) {
						if(ex.getResourceType().getResourceTypeName().equalsIgnoreCase(res.getResourceType().getResourceTypeName())) {
							exists = true;
							break;
						}
					}
					if(exists) {
						continue;
					}
				}
				else {
					saveResource(res);
				}
			}
		}
	}
	private void saveResource(Resources res) {
		ResourceType existsResourceType = resourceTypeDao.findByResourceTypeNameIgnoreCase(res.getResourceType().getResourceTypeName());
		if(existsResourceType == null) {
			res.getResourceType().setId(UUID.randomUUID().toString());
			existsResourceType = resourceTypeDao.save(res.getResourceType());
		}
		res.setResourceType(existsResourceType);
		QuantityType existsQuantityType = quantityTypeDao.findByQuantityTypeIgnoreCase(res.getQuantityType().getQuantityType());
		if(existsQuantityType == null) {
			res.getQuantityType().setId(UUID.randomUUID().toString());
			existsQuantityType = quantityTypeDao.save(res.getQuantityType());
		}
		res.setQuantityType(existsQuantityType);
		UOM existsUom = uomDao.findByUomCodeIgnoreCase(res.getUom().getUomCode()); 
		if(existsUom == null) {
			res.getUom().setId(UUID.randomUUID().toString());
			existsUom = uomDao.save(res.getUom());
		}
		res.setUom(existsUom);
		res.setId(UUID.randomUUID().toString());
		resourceDao.save(res);
	}
}