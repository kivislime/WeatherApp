package org.kivislime.weatherapp.location.dto;

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
