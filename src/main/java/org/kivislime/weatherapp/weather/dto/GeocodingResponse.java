package org.kivislime.weatherapp.weather.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GeocodingResponse {
    private String name;
    private Double lat;
    private Double lon;

    private String country;
    private String state;
}
