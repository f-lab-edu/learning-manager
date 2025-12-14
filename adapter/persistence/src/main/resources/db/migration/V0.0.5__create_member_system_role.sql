-- member_system_role: 회원 시스템 역할 (다중 역할 지원)
CREATE TABLE member_system_role
(
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'PK',
    created_at  DATETIME(6) NOT NULL COMMENT '생성 일시',
    created_by  BIGINT      NOT NULL COMMENT '생성자 ID',

    member_id   BIGINT      NOT NULL COMMENT '회원 ID (FK)',
    system_role VARCHAR(20) NOT NULL COMMENT '시스템 역할 (ADMIN, MEMBER, SUPERVISOR, OPERATOR, REGISTRAR, AUDITOR)',

    CONSTRAINT pk_member_system_role PRIMARY KEY (id),
    CONSTRAINT uk_member_system_role UNIQUE (member_id, system_role),
    CONSTRAINT fk_member_system_role_member FOREIGN KEY (member_id) REFERENCES member (id),

    INDEX ix_member_system_role_member (member_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT = '회원의 시스템 역할을 관리하는 테이블 (다중 역할 지원)';

-- 기존 member.role 데이터를 member_system_role로 이전
INSERT INTO member_system_role (created_at, created_by, member_id, system_role)
SELECT NOW(), 0, id, role
FROM member
WHERE role IS NOT NULL;

-- member 테이블에서 role 컬럼 삭제
ALTER TABLE member
    DROP COLUMN role;
