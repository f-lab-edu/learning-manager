CREATE TABLE if not exists credential
(
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Credential 고유 ID',
    account_id   BIGINT       NOT NULL COMMENT '계정 ID (FK)',
    type         VARCHAR(20)  NOT NULL COMMENT '인증 유형 (PASSWORD, GOOGLE, KAKAO, GITHUB)',
    secret       VARCHAR(255) NOT NULL COMMENT '인증 정보 (해시된 비밀번호 또는 OAuth provider ID)',
    last_used_at DATETIME(6)  NULL COMMENT '마지막 사용 일시',

    CONSTRAINT pk_credential PRIMARY KEY (id),
    CONSTRAINT fk_credential_account FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE CASCADE,
    CONSTRAINT uk_credential_account_type UNIQUE (account_id, type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='계정의 인증 수단을 관리하는 테이블';

INSERT INTO credential (account_id, type, secret, last_used_at)
SELECT id, 'PASSWORD', password, NULL
FROM account
WHERE password != '';

ALTER TABLE account
    DROP COLUMN password