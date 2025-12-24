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
import com.ksv.tactive.dto.CountryCityDto;
import com.ksv.tactive.location.dao.CitiesDao;
import com.ksv.tactive.location.dao.CountryDao;
import com.ksv.tactive.location.entity.Cities;
import com.ksv.tactive.location.entity.Country;

@Component
public class CountryDataLoader implements MasterDataLoader<Country>{
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private CountryDao countryDao;
	
	@Autowired
	private CitiesDao cityDao;

	@Override
	public void loadData() throws IOException, DataAccessException {
		try (InputStream inputStream = getClass().getResourceAsStream("/data/country_city_data.json")) {
			if (inputStream == null) {
				throw new IOException("Resource file 'countries.json' not found");
			}
			List<CountryCityDto> countriesData = objectMapper.readValue(inputStream, new TypeReference<>() {});
			for(CountryCityDto country : countriesData) {
				Country savedCountry = countryDao.findByCountry(country.getCountry());
				Cities city = new Cities();
				if(savedCountry != null ) {
					city.setCity(country.getCity());
					city.setCountry(savedCountry);
					saveCity(city);
				}
				else {
					Country countries = new Country();
					countries.setId(UUID.randomUUID().toString());
					countries.setCountry(country.getCountry());
					countries.setCountryCode(country.getCountryCode());
					savedCountry = countryDao.save(countries);
					city.setCity(country.getCity());
					city.setCountry(savedCountry);
					saveCity(city);
				}
			}
		}
	}
	private void saveCity(Cities city) {
		Cities savedCity = cityDao.findByCityAndCountryId(city.getCity(), city.getCountry().getId());
		if(savedCity == null) {
			city.setId(UUID.randomUUID().toString());
			city.setActive(true);
			cityDao.save(city);
		}	
	}
}
