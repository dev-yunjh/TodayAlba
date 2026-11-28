CREATE TABLE login_sessions (
    id                 BIGINT     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id            BINARY(16) NOT NULL,
    refresh_token_hash BINARY(32) NOT NULL COMMENT '리프레시 토큰의 SHA-256 해시',
    expires_at         DATETIME   NOT NULL COMMENT '리프레시 토큰 만료 시각',

    CONSTRAINT fk_login_sessions_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_login_sessions_refresh_token_hash
        UNIQUE (refresh_token_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
