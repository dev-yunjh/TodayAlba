package com.example.albaz.user.scheduler;

import com.example.albaz.user.repository.EmailVerificationRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailVerificationCleanupJob {
    private final EmailVerificationRepository verifications;

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void deleteExpiredVerifications() {
        verifications.deleteExpired(Instant.now());
    }
}
