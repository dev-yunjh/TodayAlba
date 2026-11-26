CREATE TABLE users (
                       id                BINARY(16)   NOT NULL PRIMARY KEY,
                       email_hash        VARCHAR(255) NOT NULL UNIQUE,
                       password_hash     VARCHAR(255) NOT NULL,
                       nickname          VARCHAR(30)  NOT NULL UNIQUE,
                       role              VARCHAR(20)  NOT NULL DEFAULT 'USER',
                       status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
                       profile_image_url VARCHAR(500) NULL,
                       suspended_until   DATETIME     NULL,
                       last_login_at     DATETIME     NULL,
                       created_at        DATETIME     NOT NULL,
                       updated_at        DATETIME     NOT NULL,
                       deleted_at        DATETIME     NULL,

                       CONSTRAINT chk_users_role
                           CHECK(role IN('USER', 'ADMIN')),

                       CONSTRAINT chk_users_status
                           CHECK (status IN ('ACTIVE', 'SUSPENDED', 'BANNED', 'WITHDRAWN'))
);