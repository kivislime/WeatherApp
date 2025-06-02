package org.kivislime.weather.dto;

import lombok.Value;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Value
public class SessionDto {
    UUID id;
    Long userId;
    Instant expiresAt;
}
