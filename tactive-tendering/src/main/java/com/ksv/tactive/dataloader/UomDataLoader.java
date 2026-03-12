package com.ksv.tactive.dataloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import com.ksv.tactive.project.entity.Uom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksv.tactive.project.dao.UomDao;

@Component
public class UomDataLoader implements MasterDataLoader<Uom> {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UomDao uomDao;

    @Override
    public void loadData() throws IOException, DataAccessException {
        try (InputStream inputStream = getClass().getResourceAsStream("/data/uom.json")) {
            if (inputStream == null) {
                throw new IOException("Resource file 'uom.json' not found");
            }
            List<Uom> uomData = objectMapper.readValue(inputStream, new TypeReference<List<Uom>>() {});
            for (Uom uom : uomData) {
                saveUom(uom);
            }
        }
    }
    private void saveUom(Uom uomInput) {
        Uom existingUom = uomDao.findByUomCode(uomInput.getUomCode());
        if (existingUom == null) {
            uomInput.setActive(true);
            uomDao.save(uomInput);
        }
    }
}