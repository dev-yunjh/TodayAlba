package com.example.albaz.user.repository;

import com.example.albaz.user.domain.EmailVerification;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<EmailVerification> findByEmailHash(String emailHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<EmailVerification> findByTokenHash(String tokenHash);

    @Transactional
    @Modifying
    @Query("""
            delete from EmailVerification v
            where v.codeExpiresAt <= :now
              and (v.tokenExpiresAt is null or v.tokenExpiresAt <= :now)
            """)
    int deleteExpired(@Param("now") Instant now);
}
