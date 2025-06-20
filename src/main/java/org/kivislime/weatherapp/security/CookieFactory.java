package org.kivislime.weatherapp.security;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

@Component
public class CookieFactory {
    private final CookieProperties cookieProperties;

    public CookieFactory(CookieProperties cookieProperties) {
        this.cookieProperties = cookieProperties;
    }

    public Cookie createCookie(String value) {
        Cookie cookie = new Cookie(cookieProperties.getCookieName(), value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(cookieProperties.getCookieMaxAge());
        return cookie;
    }

    public Cookie deleteCookie(String value) {
        Cookie cookie = new Cookie(cookieProperties.getCookieName(), value);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        return cookie;
    }
}
