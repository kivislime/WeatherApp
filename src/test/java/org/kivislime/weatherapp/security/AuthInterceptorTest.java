package org.kivislime.weatherapp.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kivislime.weatherapp.user.dto.UserDto;
import org.kivislime.weatherapp.session.exception.SessionNotFoundException;
import org.kivislime.weatherapp.session.service.SessionService;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock private SessionService sessionService;
    @Mock private CookieProperties cookieProperties;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    private final Object dummyHandler = new Object();
    private AuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new AuthInterceptor(sessionService, cookieProperties);
    }

    @Test
    void whenNoCookies_ShouldRedirectToSignInAndReturnFalse() throws Exception {
        when(request.getCookies()).thenReturn(null);
        when(request.getContextPath()).thenReturn("/app");

        boolean result = interceptor.preHandle(request, response, dummyHandler);

        verify(response).sendRedirect("/app/sign-in");
        assertFalse(result);
    }

    @Test
    void whenCookiesExistButSessionCookieMissing_ShouldRedirectToSignInAndReturnFalse() throws Exception {
        Cookie other = new Cookie("OTHER_COOKIE", "value");
        when(request.getCookies()).thenReturn(new Cookie[]{other});
        when(request.getContextPath()).thenReturn("/app");
        when(cookieProperties.getCookieName()).thenReturn("SESSION_ID");

        boolean result = interceptor.preHandle(request, response, dummyHandler);

        verify(response).sendRedirect("/app/sign-in");
        assertFalse(result);
    }

    @Test
    void whenSessionServiceThrows_ShouldPropagateException() throws IOException {
        Cookie sessionCookie = new Cookie("SESSION_ID", "invalid-uuid");
        when(request.getCookies()).thenReturn(new Cookie[]{sessionCookie});
        when(cookieProperties.getCookieName()).thenReturn("SESSION_ID");

        when(sessionService.findUserDtoBySessionUuid("invalid-uuid"))
                .thenThrow(new SessionNotFoundException("invalid-uuid"));

        SessionNotFoundException ex = assertThrows(
                SessionNotFoundException.class,
                () -> interceptor.preHandle(request, response, dummyHandler)
        );

        assertEquals("invalid-uuid", ex.getMessage());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void whenValidSessionCookie_ShouldSetUserAttributeAndReturnTrue() throws Exception {
        Cookie sessionCookie = new Cookie("SESSION_ID", "valid-uuid");
        when(request.getCookies()).thenReturn(new Cookie[]{sessionCookie});
        when(cookieProperties.getCookieName()).thenReturn("SESSION_ID");

        UserDto fakeUser = new UserDto(42L, "alice");
        when(sessionService.findUserDtoBySessionUuid("valid-uuid"))
                .thenReturn(fakeUser);

        boolean result = interceptor.preHandle(request, response, dummyHandler);

        ArgumentCaptor<UserDto> captor = ArgumentCaptor.forClass(UserDto.class);
        verify(request).setAttribute(eq("user"), captor.capture());
        assertThat(captor.getValue()).isSameAs(fakeUser);

        assertTrue(result);
        verify(response, never()).sendRedirect(anyString());
    }
}
