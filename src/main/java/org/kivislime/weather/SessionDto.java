package org.kivislime.weather;

import lombok.Value;

import java.util.Date;
import java.util.UUID;

@Value
public class SessionDto {
    UUID id;
    Long userId;
    Date expiresAt;
}
