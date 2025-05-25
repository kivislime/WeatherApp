package org.kivislime.weather;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component("authRedirectFilter")
public class AuthRedirectFilter implements Filter {
    private final SessionService sessionService;
    private final CookieProperties cookieProperties;

    public AuthRedirectFilter(SessionService sessionService, CookieProperties cookieProperties) {
        this.sessionService = sessionService;
        this.cookieProperties = cookieProperties;
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        String path = req.getRequestURI().substring(req.getContextPath().length());
        //TODO: захардкожено ведь?
        if (path.equals("/") ||
                path.equals("/sign-in") ||
                path.equals("/sign-up") ||
                path.equals("/registration") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/")) {
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
