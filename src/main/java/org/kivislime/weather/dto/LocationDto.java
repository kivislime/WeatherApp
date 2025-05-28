package org.kivislime.weather.dto;

import lombok.Value;

@Value
public class LocationDto {
    Long id;
    String name;
    Long userId;
    Double lat;
    Double lon;
    Double temp;
}
