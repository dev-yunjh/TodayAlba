package com.example.albaz.user.repository;

import com.example.albaz.user.domain.LoginSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface LoginSessionRepository extends JpaRepository<LoginSession, Long> {
    @Modifying
    @Query("delete from LoginSession s where s.refreshTokenHash = :tokenHash")
    void deleteByTokenHash(byte[] tokenHash);
}
