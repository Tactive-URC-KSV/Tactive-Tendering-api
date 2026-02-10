package com.ksv.tactive.dataloader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksv.tactive.dto.Cities;
import com.ksv.tactive.dto.Country;
import com.ksv.tactive.dto.CountryDataDto;
import com.ksv.tactive.dto.States;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class LocationMemoryEngine {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<Country> countryList = new ArrayList<>();
    private final Map<String, List<States>> statesByCountryId = new HashMap<>();
    private final Map<String, List<Cities>> citiesByStateId = new HashMap<>();

    @PostConstruct
    public void loadData() {
        try (InputStream inputStream = getClass().getResourceAsStream("/data/country_city_data.json")) {
            if (inputStream == null) return;

            List<CountryDataDto> rawData = objectMapper.readValue(inputStream, new TypeReference<>() {});

            for (CountryDataDto cDto : rawData) {
                Country country = new Country();
                String countryKey = cDto.getCountryCode();
                country.setId(UUID.nameUUIDFromBytes(countryKey.getBytes()).toString());
                country.setCountry(cDto.getCountry());
                country.setCountryCode(cDto.getCountryCode());
                country.setPhoneNumberCode(cDto.getPhoneNumberCode());
                country.setActive(cDto.isActive());

                countryList.add(country);

                if (cDto.getStates() != null) {
                    List<States> stateList = new ArrayList<>();

                    for (CountryDataDto.StateDataDto sDto : cDto.getStates()) {
                        States state = new States();
                        String stateKey = sDto.getState() + "_" + cDto.getCountryCode();
                        state.setId(UUID.nameUUIDFromBytes(stateKey.getBytes()).toString());
                        state.setState(sDto.getState());
                        state.setActive(sDto.isActive());
                        state.setCountry(country);

                        stateList.add(state);

                        if (sDto.getCities() != null) {
                            List<Cities> cityList = new ArrayList<>();
                            for (CountryDataDto.CityDataDto cityDto : sDto.getCities()) {
                                Cities city = new Cities();
                                String cityKey = cityDto.getCity() + "_" + sDto.getState() + "_" + cDto.getCountryCode();
                                city.setId(UUID.nameUUIDFromBytes(cityKey.getBytes()).toString());
                                city.setCity(cityDto.getCity());
                                city.setActive(cityDto.isActive());
                                city.setStates(state);
                                city.setCountry(country);

                                cityList.add(city);
                            }
                            citiesByStateId.put(state.getId(), cityList);
                        }
                    }
                    statesByCountryId.put(country.getId(), stateList);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load location data", e);
        }
    }

    public List<Country> findAllCountries() {
        return countryList;
    }

    public List<States> findStatesByCountryId(String countryId) {
        return statesByCountryId.getOrDefault(countryId, Collections.emptyList());
    }

    public List<Cities> findCitiesByStateId(String stateId) {
        return citiesByStateId.getOrDefault(stateId, Collections.emptyList());
    }
}