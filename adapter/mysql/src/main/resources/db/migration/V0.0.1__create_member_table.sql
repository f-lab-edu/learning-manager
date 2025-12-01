CREATE TABLE member
(
    -- AbstractEntity
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '멤버 고유 ID',
    created_at        DATETIME(6)  NOT NULL COMMENT '생성 일시',
    last_modified_at  DATETIME(6)  NULL COMMENT '수정 일시',
    created_by        BIGINT       NOT NULL COMMENT '생성자 ID',
    last_modified_by  BIGINT       NULL COMMENT '수정자 ID',

    -- Member 엔티티 필드
    nickname          VARCHAR(20)  NOT NULL COMMENT '닉네임',
    role              VARCHAR(20)  NOT NULL COMMENT '시스템 역할 (MEMBER, ADMIN)',
    status            VARCHAR(20)  NOT NULL COMMENT '멤버 상태 (ACTIVE, INACTIVE, WITHDRAWN, BANNED)',
    profile_image_url VARCHAR(255) NULL COMMENT '프로필 이미지 URL',
    self_introduction VARCHAR(255) NULL COMMENT '자기 소개',

    CONSTRAINT pk_member PRIMARY KEY (id),
    CONSTRAINT UK_member_nickname UNIQUE (nickname)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='사용자 정보를 관리하는 테이블';
