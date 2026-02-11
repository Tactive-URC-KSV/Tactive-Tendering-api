package com.ksv.tactive.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryDataDto {
    private String country;
    private String countryCode;
    private String phoneNumberCode;
    private boolean active;
    private List<StateDataDto> states; // Maintains relation to States

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StateDataDto {
        private String state;
        private boolean active;
        private List<CityDataDto> cities; // Maintains relation to Cities
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CityDataDto {
        private String city;
        private boolean active;
    }
}
