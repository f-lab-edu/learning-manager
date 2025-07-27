CREATE TABLE course
(
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '코스 고유 ID',
    created_at       DATETIME(6)  NOT NULL COMMENT '생성 일시',
    last_modified_at DATETIME(6)  NULL COMMENT '수정 일시',
    created_by       BIGINT       NOT NULL COMMENT '생성자 ID',
    last_modified_by BIGINT       NULL COMMENT '수정자 ID',

    title            VARCHAR(255) NOT NULL COMMENT '코스명', # 대소문자 구분하지 않는 코스명
    `description`    VARCHAR(255) NULL COMMENT '코스에 대한 간략한 설명',

    CONSTRAINT pk_course PRIMARY KEY (id),
    CONSTRAINT UK_course_title UNIQUE (title)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='스터디 과정을 관리하는 테이블';