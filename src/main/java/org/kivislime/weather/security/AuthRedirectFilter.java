package org.kivislime.weather.security;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.kivislime.weather.service.SessionService;
import org.kivislime.weather.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component("authRedirectFilter")
public class AuthRedirectFilter implements Filter {
    private final SessionService sessionService;
    private final CookieProperties cookieProperties;
    private final String rawPublicUrls;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public AuthRedirectFilter(SessionService sessionService,
                              CookieProperties cookieProperties,
                              @Value("${security.public-urls}")
                              String rawPublicUrls) {
        this.sessionService = sessionService;
        this.cookieProperties = cookieProperties;
        this.rawPublicUrls = rawPublicUrls;
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        String path = req.getRequestURI().substring(req.getContextPath().length());

        List<String> publicUrls = Arrays.stream(rawPublicUrls.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        boolean isPublic = publicUrls.stream().anyMatch(publicUrl -> matcher.match(publicUrl, path));
        if (isPublic) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            resp.sendRedirect(req.getContextPath() + "/sign-in");
            return;
        }

        Optional<Cookie> opt = Arrays.stream(cookies)
                .filter(c -> c.getName().equals(cookieProperties.getCookieName()))
                .findFirst();

        if (opt.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/sign-in");
            return;
        }

        UserDto userDto = sessionService.findUserDtoBySessionUuid(opt.get().getValue());
        req.setAttribute("user", userDto);

        filterChain.doFilter(servletRequest, servletResponse);
    }

}
