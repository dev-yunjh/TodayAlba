CREATE TABLE email_verifications (
    email_hash          VARCHAR(64) NOT NULL PRIMARY KEY,
    version             BIGINT NOT NULL,
    code_hash           VARCHAR(64),
    code_expires_at     TIMESTAMP(6),
    resend_after        TIMESTAMP(6),
    remaining_attempts  INT NOT NULL,
    token_hash          VARCHAR(64) UNIQUE,
    token_expires_at    TIMESTAMP(6) NULL,

    CONSTRAINT chk_email_verifications_remaining_attempts
         CHECK (remaining_attempts BETWEEN 0 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
