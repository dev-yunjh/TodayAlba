package com.example.albaz.user.repository;

import com.example.albaz.user.domain.User;
import com.example.albaz.user.domain.Status;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailHash(String emailHash);

    @Modifying
    @Query("""
            update User u set u.lastLoginAt = :now, u.updatedAt = :now
            where u.id = :id and u.status = :status and u.deletedAt is null
            """)
    int recordLogin(UUID id, Status status, LocalDateTime now);
}
