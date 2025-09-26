package com.ksv.tactive.dataloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksv.tactive.project.dao.ActivityGroupDao;
import com.ksv.tactive.project.dao.CostCodeTypeDao;
import com.ksv.tactive.project.entity.ActivityGroup;
import com.ksv.tactive.project.entity.CostCodeType;

@Component
public class ActivityGroupLoader implements MasterDataLoader<ActivityGroup> {
    private static final Logger logger = LoggerFactory.getLogger(ActivityGroupLoader.class);

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ActivityGroupDao activityGroupDao;
    @Autowired
    private CostCodeTypeDao costCodeTypeDao;

    @Transactional
    @Override
    public void loadData() throws IOException {
        logger.info("Starting data load for ActivityGroup");
        try (InputStream inputStream = getClass().getResourceAsStream("/data/standard-costcodes.json")) {
            if (inputStream == null) {
                throw new IOException("Resource file 'standard-costcodes.json' not found");
            }
            List<ActivityGroup> costCodes = objectMapper.readValue(inputStream, new TypeReference<>() {});
            
                for (ActivityGroup costCode : costCodes) {
                	if(activityGroupDao.findByActivityNameIgnoreCase(costCode.getActivityName()) != null) {
						continue;
					}
                	CostCodeType costCodeType = findCostCodeTypeByName(costCode.getCostCodeType().getCostCodeName());
                    costCode.setId(UUID.randomUUID().toString());
                    costCode.setCostCodeType(costCodeType);
                    activityGroupDao.save(costCode);
                }
                
        }
    }
    private CostCodeType findCostCodeTypeByName(String name) {
    	CostCodeType costCodeType = costCodeTypeDao.findByCostCodeName(name);
    	if (costCodeType == null) {
			costCodeType = new CostCodeType();
			costCodeType.setId(UUID.randomUUID().toString());
			costCodeType.setCostCodeName(name);
			costCodeTypeDao.save(costCodeType);
		}
		return costCodeType;
		
	} 
}