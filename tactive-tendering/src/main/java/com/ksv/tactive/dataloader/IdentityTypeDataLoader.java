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
import com.ksv.tactive.project.dao.IdentityTypeDao;
import com.ksv.tactive.project.entity.IdentityType;

@Component
public class IdentityTypeDataLoader implements MasterDataLoader<IdentityType> {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdentityTypeDao identityTypeDao;

    @Override
    public void loadData() throws IOException, DataAccessException {
        try (InputStream inputStream = getClass().getResourceAsStream("/data/identitytype.json")) {
            if (inputStream == null) {
                throw new IOException("Resource file 'identitytype.json' not found");
            }
            List<IdentityType> identityTypes = objectMapper.readValue(inputStream, new TypeReference<List<IdentityType>>() {});
            for (IdentityType identityType : identityTypes) {
                saveIdentityType(identityType);
            }
        }
    }
    private void saveIdentityType(IdentityType identityTypeInput) {
        IdentityType existing = identityTypeDao.findByIdType(identityTypeInput.getIdType());
        if (existing == null) {
            if (!identityTypeInput.isActive()) {
                identityTypeInput.setActive(true);
            }
            identityTypeDao.save(identityTypeInput);
        }
    }
}