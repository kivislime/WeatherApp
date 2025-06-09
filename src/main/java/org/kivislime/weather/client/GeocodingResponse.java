package org.kivislime.weather.client;

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
