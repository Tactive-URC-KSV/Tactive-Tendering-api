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
import com.ksv.tactive.currency.dao.CurrencyDao;
import com.ksv.tactive.currency.entity.Currency;

import jakarta.transaction.Transactional;
@Component
public class CurrencyLoader implements MasterDataLoader<Currency> {
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private CurrencyDao currencyDao;
	
	@Transactional
	@Override
	public void loadData() throws IOException, DataAccessException {
		try (InputStream inputStream = getClass().getResourceAsStream("/data/currencies.json")) {
			if (inputStream == null) {
				throw new IOException("Resource file 'currencies.json' not found");
			}
			List<Currency> currencies = objectMapper.readValue(inputStream, new TypeReference<>() {});
			for (Currency currencyDto : currencies) {
				if (currencyDao.findByCurrencyCode(currencyDto.getCurrencyCode()) != null) {
					continue;
				}
				Currency currency = new Currency();
				currency.setId(UUID.randomUUID().toString());
				currency.setCurrencyCode(currencyDto.getCurrencyCode());
				currency.setCurrencyName(currencyDto.getCurrencyName());
				currency.setSymbol(currencyDto.getSymbol());
				currency.setActive(true);
				currencyDao.save(currency);
			}
			
		}
	}
}
