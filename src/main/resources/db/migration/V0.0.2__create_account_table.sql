CREATE TABLE account
(
    -- AbstractEntity
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '계정 고유 ID',
    created_at       DATETIME(6)  NOT NULL COMMENT '생성 일시',
    last_modified_at DATETIME(6)  NULL COMMENT '수정 일시',
    created_by       BIGINT       NOT NULL COMMENT '생성자 ID',
    last_modified_by BIGINT       NULL COMMENT '수정자 ID',

    -- Account 엔티티 필드
    member_id        BIGINT       NOT NULL COMMENT '멤버 고유 ID',
    status           VARCHAR(20)  NOT NULL COMMENT '계정 상태 (PENDING, ACTIVE, INACTIVE 등)',
    email            VARCHAR(255) NOT NULL COMMENT '이메일 주소',
    password VARCHAR(255) NOT NULL COMMENT '암호화된 비밀 번호' collate utf8mb4_bin,
    -- 제약 조건 설정
    CONSTRAINT pk_account PRIMARY KEY (id),
    CONSTRAINT UK_account_email UNIQUE (`email`),
    CONSTRAINT FK_account_on_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='사용자의 로그인 계정 정보를 관리하는 테이블';
