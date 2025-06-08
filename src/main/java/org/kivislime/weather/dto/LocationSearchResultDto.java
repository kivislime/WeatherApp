package org.kivislime.weather.dto;

import lombok.Value;

@Value
public class LocationSearchResultDto {
    String name;
    Double lat;
    Double lon;

    String country;
    String state;
}
