package org.kivislime.weather;

import lombok.Value;

@Value
public class LocationRegistrationDto {
    String name;
    Long userId;
    Double latitude;
    Double longitude;
}
