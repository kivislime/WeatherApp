package org.kivislime.weather.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kivislime.weather.dto.UserDto;
import org.kivislime.weather.service.SessionService;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class AuthRedirectFilterTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private CookieProperties cookieProperties;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private AuthRedirectFilter filter;

    private static final String RAW_PUBLIC_URLS = "/public/**,/static/*";

    @BeforeEach
    void setUp() {
        filter = new AuthRedirectFilter(
                sessionService,
                cookieProperties,
                RAW_PUBLIC_URLS
        );
        when(request.getContextPath()).thenReturn("/app");
    }

    @Test
    void whenRequestIsPublic_ShouldDoFilterAndNotRedirect() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/app/public/home");

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void whenProtectedAndNoCookies_ShouldRedirectToSignIn() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/app/protected/page");
        when(request.getCookies()).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(response, times(1)).sendRedirect("/app/sign-in");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void whenProtectedAndCookieMissingName_ShouldRedirectToSignIn() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/app/dashboard");
        Cookie other = new Cookie("anotherCookie", "value");
        when(request.getCookies()).thenReturn(new Cookie[]{ other });
        when(cookieProperties.getCookieName()).thenReturn("SESSION_ID");

        filter.doFilter(request, response, filterChain);

        verify(response, times(1)).sendRedirect("/app/sign-in");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void whenProtectedAndCookiePresentButInvalidSession_ShouldPropagateException() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/app/secure");
        Cookie sessionCookie = new Cookie("SESSION_ID", "bad-uuid");
        when(request.getCookies()).thenReturn(new Cookie[]{ sessionCookie });
        when(cookieProperties.getCookieName()).thenReturn("SESSION_ID");

        when(sessionService.findUserDtoBySessionUuid("bad-uuid"))
                .thenThrow(new RuntimeException("invalid session"));

        assertThrows(RuntimeException.class,
                () -> filter.doFilter(request, response, filterChain));

        verify(response, never()).sendRedirect(anyString());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void whenProtectedAndCookieValid_ShouldSetUserAndDoFilter() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/app/dashboard");
        Cookie sessionCookie = new Cookie("SESSION_ID", "valid-uuid");
        when(request.getCookies()).thenReturn(new Cookie[]{ sessionCookie });
        when(cookieProperties.getCookieName()).thenReturn("SESSION_ID");

        UserDto fakeUser = new UserDto(42L, "alice");
        when(sessionService.findUserDtoBySessionUuid("valid-uuid")).thenReturn(fakeUser);

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<UserDto> captor = ArgumentCaptor.forClass(UserDto.class);
        verify(request, times(1)).setAttribute(eq("user"), captor.capture());
        assertThat(captor.getValue()).isSameAs(fakeUser);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }
}
