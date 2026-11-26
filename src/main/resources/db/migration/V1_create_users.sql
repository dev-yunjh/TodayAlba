CREATE TABLE users (
                       id                BINARY(16)   NOT NULL PRIMARY KEY,
                       email_hash        VARCHAR(255) NOT NULL UNIQUE,
                       password_hash     VARCHAR(255) NOT NULL,
                       nickname          VARCHAR(30)  NOT NULL UNIQUE,
                       role              VARCHAR(20)  NOT NULL DEFAULT 'USER',
                       membership_type   VARCHAR(20)  NOT NULL DEFAULT 'FREE',
                       status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
                       profile_image_url VARCHAR(500) NULL,
                       suspended_until   DATETIME     NULL,
                       last_login_at     DATETIME     NULL,
                       created_at        DATETIME     NOT NULL,
                       updated_at        DATETIME     NOT NULL,
                       deleted_at        DATETIME     NULL,

    -- 회원 권한 검증
                       CONSTRAINT chk_users_role
                           CHECK (role IN ('USER', 'ADMIN')),

    -- 회원 이용 등급 검증
                       CONSTRAINT chk_users_membership_type
                           CHECK (membership_type IN ('FREE', 'PLUS')),

    -- 회원 계정 상태 검증
                       CONSTRAINT chk_users_status
                           CHECK (status IN (
                                             'ACTIVE', 'SUSPENDED', 'BANNED', 'WITHDRAWN'
                               ))

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='회원 정보 관리';