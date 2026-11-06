package com.anondev.platform.modules.matching.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvitationExpiryScheduler {
    @Scheduled(cron = "0 */15 * * * *")
    public void sweepExpiredInvitations() {
        // Scheduler sweeps pending invitations past expiresAt and releases holds (BR-07)
    }

    @Scheduled(cron = "0 0 * * * *")
    public void checkOverdueStatusDeadlines() {
        // Scheduler checks 14-day update deadlines (BR-09)
    }
}
