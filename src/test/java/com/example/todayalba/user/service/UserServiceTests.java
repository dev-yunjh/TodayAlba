package com.example.todayalba.user.service;

import com.example.todayalba.user.dto.UserCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UserServiceTests {
    @Autowired
    private UserService service;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void storesHashesAndRejectsTheSameEmailWithDifferentCase() {
        var request = new UserCreateRequest("Member@example.com", "sample password", "member");
        assertThat(service.register(request)).isNotNull();
        var row = jdbc.queryForMap("select email_hash, password_hash from users where nickname = ?", "member");
        assertThat((String) row.get("email_hash")).matches("[0-9a-f]{64}");
        var encoder = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        assertThat(encoder.matches(request.password(), (String) row.get("password_hash"))).isTrue();
        var duplicate = new UserCreateRequest("member@example.com", "another password", "another");
        assertThatThrownBy(() -> service.register(duplicate)).isInstanceOf(DataIntegrityViolationException.class);
    }
}
