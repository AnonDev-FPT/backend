package com.anondev.platform.modules.matching.service;

import com.anondev.platform.modules.company.repository.CompanyRepository;
import com.anondev.platform.modules.matching.entity.InterviewInvitation;
import com.anondev.platform.modules.matching.repository.InterviewInvitationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvitationExpiryScheduler {

    private final InterviewInvitationRepository invitationRepository;
    private final MatchingUnlockService matchingUnlockService;
    private final CompanyRepository companyRepository;

    @Scheduled(cron = "0 */15 * * * *")
    public void sweepExpiredInvitations() {
        Instant now = Instant.now();
        List<InterviewInvitation> expiredList = invitationRepository.findByInvitationStatusAndExpiresAtBefore("PENDING", now);
        if (!expiredList.isEmpty()) {
            log.info("Found {} pending invitations past expiry deadline. Processing release holds...", expiredList.size());
            for (InterviewInvitation invitation : expiredList) {
                try {
                    matchingUnlockService.expireInvitation(invitation);
                } catch (Exception e) {
                    log.error("Failed to expire invitation ID {}: {}", invitation.getId(), e.getMessage(), e);
                }
            }
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void checkOverdueStatusDeadlines() {
        Instant now = Instant.now();
        List<InterviewInvitation> overdueInvitations = invitationRepository.findOverdueStatusInvitations(now);
        if (!overdueInvitations.isEmpty()) {
            log.warn("Found {} unlocked invitations with overdue 14-day candidate status updates. Blocking companies (BR-09)...", overdueInvitations.size());
            for (InterviewInvitation inv : overdueInvitations) {
                try {
                    companyRepository.findById(inv.getCompanyId()).ifPresent(company -> {
                        if (!Boolean.TRUE.equals(company.getIsBlockedByOverdueStatus())) {
                            company.setIsBlockedByOverdueStatus(true);
                            companyRepository.save(company);
                            log.warn("Company {} ({}) has been BLOCKED due to overdue status update on invitation {}",
                                    company.getCompanyName(), company.getId(), inv.getId());
                        }
                    });
                } catch (Exception e) {
                    log.error("Failed to apply overdue penalty for company of invitation {}: {}", inv.getId(), e.getMessage(), e);
                }
            }
        }
    }
}
