-- ============================================================
-- Member Domain: member, account, credential
-- ============================================================

-- --------------------------------------
-- member: 사용자 정보
-- --------------------------------------
CREATE TABLE member
(
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '멤버 고유 ID',
    created_at        DATETIME(6)  NOT NULL COMMENT '생성 일시',
    last_modified_at  DATETIME(6)  NULL COMMENT '수정 일시',
    created_by        BIGINT       NOT NULL COMMENT '생성자 ID',
    last_modified_by  BIGINT       NULL COMMENT '수정자 ID',
    version           BIGINT       NOT NULL DEFAULT 0 COMMENT '낙관적 락 버전',

    nickname          VARCHAR(20)  NOT NULL COMMENT '닉네임',
    role              VARCHAR(20)  NOT NULL COMMENT '시스템 역할 (MEMBER, ADMIN)',
    status            VARCHAR(20)  NOT NULL COMMENT '멤버 상태 (PENDING, ACTIVE, INACTIVE, WITHDRAWN, BANNED)',
    primary_email     VARCHAR(255) NOT NULL COMMENT '대표 이메일 주소',
    profile_image_url VARCHAR(255) NULL COMMENT '프로필 이미지 URL',
    self_introduction VARCHAR(255) NULL COMMENT '자기 소개',

    CONSTRAINT pk_member PRIMARY KEY (id),
    CONSTRAINT uk_member_nickname UNIQUE (nickname)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT = '사용자 정보를 관리하는 테이블';

-- --------------------------------------
-- account: 로그인 계정
-- --------------------------------------
CREATE TABLE account
(
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '계정 고유 ID',
    created_at       DATETIME(6)  NOT NULL COMMENT '생성 일시',
    last_modified_at DATETIME(6)  NULL COMMENT '수정 일시',
    created_by       BIGINT       NOT NULL COMMENT '생성자 ID',
    last_modified_by BIGINT       NULL COMMENT '수정자 ID',
    version          BIGINT       NOT NULL DEFAULT 0 COMMENT '낙관적 락 버전',

    member_id        BIGINT       NOT NULL COMMENT '멤버 고유 ID',
    status           VARCHAR(20)  NOT NULL COMMENT '계정 상태 (PENDING, ACTIVE, INACTIVE 등)',
    email            VARCHAR(255) NOT NULL COMMENT '이메일 주소',

    CONSTRAINT pk_account PRIMARY KEY (id),
    CONSTRAINT uk_account_email UNIQUE (email),
    CONSTRAINT fk_account_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT = '사용자의 로그인 계정 정보를 관리하는 테이블';

-- --------------------------------------
-- credential: 인증 수단 (Account의 ElementCollection)
-- --------------------------------------
CREATE TABLE credential
(
    account_id   BIGINT       NOT NULL COMMENT '계정 ID (FK, PK)',
    type         VARCHAR(20)  NOT NULL COMMENT '인증 유형 (PASSWORD, GOOGLE, KAKAO, GITHUB)',
    secret       VARCHAR(255) NOT NULL COMMENT '인증 정보 (해시된 비밀번호 또는 OAuth provider ID)',
    last_used_at DATETIME(6)  NULL COMMENT '마지막 사용 일시',

    CONSTRAINT pk_credential PRIMARY KEY (account_id, type),
    CONSTRAINT fk_credential_account FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT = '계정의 인증 수단을 관리하는 테이블 (Account에 종속된 Value Object)';
