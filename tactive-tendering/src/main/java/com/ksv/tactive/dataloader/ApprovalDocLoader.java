package com.ksv.tactive.dataloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksv.tactive.project.dao.ListOfApprovalsDao;
import com.ksv.tactive.project.entity.ListOfApprovals;

import jakarta.transaction.Transactional;

@Component
public class ApprovalDocLoader implements MasterDataLoader<ListOfApprovals>{

	 private static final Logger logger = LoggerFactory.getLogger(ActivityGroupLoader.class);

	    @Autowired
	    private ObjectMapper objectMapper;
	    @Autowired
	    private ListOfApprovalsDao approvalDao;
	
	@Transactional
	@Override
	public void loadData() throws IOException, DataAccessException {
		logger.info("Starting data load for Approval Document");
		try (InputStream inputStream = getClass().getResourceAsStream("/data/documents.json")) {
			if (inputStream == null) {
				throw new IOException("Resource file 'approval-documents.json' not found");
			}
			List<ListOfApprovals> approvals = objectMapper.readValue(inputStream, new TypeReference<>() {});
			
				for (ListOfApprovals approval : approvals) {
					if (approvalDao.findByDocumentName(approval.getDocumentName()) != null) {
						continue;
					}
					approval.setId(java.util.UUID.randomUUID().toString());
					approvalDao.save(approval);
				}
				
		}
		
	}

}
