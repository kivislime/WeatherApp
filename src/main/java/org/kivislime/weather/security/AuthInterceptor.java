package org.kivislime.weather.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.kivislime.weather.dto.UserDto;
import org.kivislime.weather.service.SessionService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final SessionService sessionService;
    private final CookieProperties cookieProperties;

    public AuthInterceptor(SessionService sessionService,
                           CookieProperties cookieProperties) {
        this.sessionService = sessionService;
        this.cookieProperties = cookieProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler
    ) throws Exception {

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            response.sendRedirect(request.getContextPath() + "/sign-in");
            return false;
        }

        String sessionId = Arrays.stream(cookies)
                .filter(c -> c.getName().equals(cookieProperties.getCookieName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (sessionId == null) {
            response.sendRedirect(request.getContextPath() + "/sign-in");
            return false;
        }

        UserDto userDto = sessionService.findUserDtoBySessionUuid(sessionId);

        request.setAttribute("user", userDto);

        return true;
    }
}
