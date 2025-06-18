package org.kivislime.weatherapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kivislime.weatherapp.session.SessionCleanupScheduler;
import org.kivislime.weatherapp.session.service.SessionService;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionCleanupSchedulerTest {

    @Mock
    private SessionService sessionService;

    private SessionCleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SessionCleanupScheduler(sessionService);
    }

    @Test
    void deleteExpiredSessions_ShouldInvokeRemoveAllExpired() {
        when(sessionService.removeAllExpired()).thenReturn(7);

        scheduler.deleteExpiredSessions();

        verify(sessionService, times(1)).removeAllExpired();
    }
}
