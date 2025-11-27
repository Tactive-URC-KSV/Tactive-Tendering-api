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
import com.ksv.tactive.company.dao.CompanyCurrencyDao;
import com.ksv.tactive.company.entity.Currency;
import com.ksv.tactive.dto.CurrencyDto;
import com.ksv.tactive.project.dao.ProjectCurrencyDao;
import jakarta.transaction.Transactional;
@Component
public class CurrencyLoader implements MasterDataLoader<Currency> {
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ProjectCurrencyDao prjCurrencyDao;
	@Autowired
	private CompanyCurrencyDao comCurrencyDao;
	@Transactional
	@Override
	public void loadData() throws IOException, DataAccessException {
		try (InputStream inputStream = getClass().getResourceAsStream("/data/currencies.json")) {
			if (inputStream == null) {
				throw new IOException("Resource file 'currencies.json' not found");
			}
			List<CurrencyDto> currencies = objectMapper.readValue(inputStream, new TypeReference<>() {});
			if (prjCurrencyDao.count() > 0 || comCurrencyDao.count() > 0) {
				return;
			}
			List<com.ksv.tactive.company.entity.Currency> companyCurrency = currencies.stream()
					.map(dto -> new com.ksv.tactive.company.entity.Currency(UUID.randomUUID().toString(),
							dto.getCurrencyName(), dto.getCurrencyCode(), dto.getSymbol(), dto.isActive()))
					.toList();
			comCurrencyDao.saveAll(companyCurrency);
			List<com.ksv.tactive.project.entity.Currency> projectCurrency = currencies.stream()
					.map(dto -> new com.ksv.tactive.project.entity.Currency(UUID.randomUUID().toString(),
							dto.getCurrencyName(), dto.getCurrencyCode(), dto.getSymbol(), dto.isActive()))
					.toList();
			prjCurrencyDao.saveAll(projectCurrency);
		}
	}
}
