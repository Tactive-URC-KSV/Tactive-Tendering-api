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
import com.ksv.tactive.project.dao.CurrencyDao;
import com.ksv.tactive.project.entity.Currency;

@Component
public class CurrencyDataLoader implements MasterDataLoader<Currency> {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CurrencyDao currencyDao;

    @Override
    public void loadData() throws IOException, DataAccessException {
        try (InputStream inputStream = getClass().getResourceAsStream("/data/currencies.json")) {
            if (inputStream == null) {
                throw new IOException("Resource file 'currency.json' not found");
            }
            List<Currency> currenciesData = objectMapper.readValue(inputStream, new TypeReference<List<Currency>>() {});
            for (Currency currency : currenciesData) {
                saveCurrency(currency);
            }
        }
    }

    private void saveCurrency(Currency currencyInput) {
        Currency existingCurrency = currencyDao.findByCurrencyCode(currencyInput.getCurrencyCode());
        if (existingCurrency == null) {
            currencyInput.setActive(true);
            currencyDao.save(currencyInput);
        }
    }
}