package org.kivislime.weather.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SessionCleanupScheduler {
    private final SessionService sessionService;
    @Scheduled(cron = "0 0 */1 * * *")
    public void deleteExpiredSessions() {
        log.info("Scheduled task: deleting expired sessions");
        int deleted = sessionService.removeAllExpired();
        log.info("Deleted expired sessions: {}", deleted);
    }
}
