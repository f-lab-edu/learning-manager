CREATE TABLE if not exists member_system_role_audit_log
(
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'PK',
    member_id    BIGINT       NOT NULL COMMENT '대상 회원 ID',
    system_role  VARCHAR(20)  NOT NULL COMMENT '변경된 역할',
    action       VARCHAR(10)  NOT NULL COMMENT '변경 유형 (GRANT, REVOKE)',
    performed_by BIGINT       NOT NULL COMMENT '수행자 ID',
    performed_at DATETIME(6)  NOT NULL COMMENT '수행 시각',
    reason       VARCHAR(500) NULL COMMENT '변경 사유',

    CONSTRAINT pk_system_role_audit_log PRIMARY KEY (id),

    INDEX ix_audit_member_id (member_id),
    INDEX ix_audit_performed_at (performed_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT = '시스템 역할 변경 이력';
