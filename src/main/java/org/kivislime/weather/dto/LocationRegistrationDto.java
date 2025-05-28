package org.kivislime.weather.dto;

import lombok.Value;

//TODO: records? затестить
@Value
public class LocationRegistrationDto {
    String name;
    Double latitude;
    Double longitude;
}
