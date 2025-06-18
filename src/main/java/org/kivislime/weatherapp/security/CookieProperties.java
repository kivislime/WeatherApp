package org.kivislime.weatherapp.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class CookieProperties {

    private final String cookieName;
    private final int cookieMaxAge;

    public CookieProperties(@Value("${cookie.name:SESSION_ID}") String cookieName,
                            @Value("${cookie.max-age.seconds:3600}") int cookieMaxAge) {
        this.cookieName = cookieName;
        this.cookieMaxAge = cookieMaxAge;
    }
}
