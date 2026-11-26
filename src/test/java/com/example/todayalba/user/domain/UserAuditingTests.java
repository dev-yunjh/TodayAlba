package com.example.todayalba.user.domain;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserAuditingTests {
    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void recordsCreationAndUpdatesModificationTime() {
        User user = new User("email-hash", "password-hash", "nickname");
        entityManager.persist(user);
        entityManager.flush();
        entityManager.refresh(user);

        var createdAt = user.getCreatedAt();
        var initialUpdatedAt = user.getUpdatedAt();
        assertThat(createdAt).isNotNull();
        assertThat(initialUpdatedAt).isNotNull();

        ReflectionTestUtils.setField(user, "nickname", "changed");
        entityManager.flush();
        entityManager.refresh(user);

        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        assertThat(user.getUpdatedAt()).isAfter(initialUpdatedAt);
    }
}
