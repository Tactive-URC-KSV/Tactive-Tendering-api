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
import com.ksv.tactive.master.dao.ProjectStatusDao;
import com.ksv.tactive.master.entity.ProjectStatus;

import jakarta.transaction.Transactional;
@Component
public class ProjectStatusLoader implements MasterDataLoader<ProjectStatus>{
	private static final Logger logger = LoggerFactory.getLogger(ProjectStatusLoader.class);
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ProjectStatusDao projectStatusDao;
	@Transactional
	@Override
	public void loadData() throws IOException, DataAccessException {
		logger.info("Starting data load for ProjectStatus");
		try(InputStream inputStream = getClass().getResourceAsStream("/data/project-status.json")){
			if(inputStream == null) {
				throw new IOException("Resource file 'project-status.json' not found");
			}
			List<ProjectStatus> projectStatuses = objectMapper.readValue(inputStream, new TypeReference<>() {});
				for(ProjectStatus projectStatus : projectStatuses) {
					if(projectStatusDao.findByStatusIgnoreCase(projectStatus.getStatus()) != null) {
						continue;
					}else {
						projectStatus.setId(UUID.randomUUID().toString());
						projectStatus.setActive(true);
						projectStatusDao.save(projectStatus);
					}
				}
		}
	}
}
