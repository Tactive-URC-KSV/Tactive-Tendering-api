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
import com.ksv.tactive.project.dao.TaxTypeDao;
import com.ksv.tactive.project.entity.TaxType;

@Component
public class TaxTypeDataLoader implements MasterDataLoader<TaxType> {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaxTypeDao taxTypeDao;

    @Override
    public void loadData() throws IOException, DataAccessException {
        try (InputStream inputStream = getClass().getResourceAsStream("/data/taxtype.json")) {
            if (inputStream == null) {
                throw new IOException("Resource file 'taxtype.json' not found");
            }

            List<TaxType> taxTypes = objectMapper.readValue(inputStream, new TypeReference<List<TaxType>>() {});

            for (TaxType taxType : taxTypes) {
                saveTaxType(taxType);
            }
        }
    }

    private void saveTaxType(TaxType taxTypeInput) {
        TaxType existing = taxTypeDao.findByTaxType(taxTypeInput.getTaxType());

        if (existing == null) {
            if (!taxTypeInput.isActive()) {
                taxTypeInput.setActive(true);
            }
            taxTypeDao.save(taxTypeInput);
        }
    }
}