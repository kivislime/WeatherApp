package org.kivislime.weatherapp.location.dto;

import lombok.Value;

@Value
public class LocationWeatherDto {
    Long    id;
    String  name;
    Long    userId;
    Double  lat;
    Double  lon;

    Double   temp;
    Double   feelsLike;
    String   description;
    Integer  humidity;
    String   iconUrl;
}
