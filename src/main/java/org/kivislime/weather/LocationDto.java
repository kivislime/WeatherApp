package org.kivislime.weather;

import lombok.Value;

@Value
public class LocationDto {
    Long id;
    String name;
    Long userId;
    Double latitude;
    Double longitude;
}
