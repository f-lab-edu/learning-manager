CREATE TABLE curriculum
(
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '커리큘럼 고유 ID',
    created_at       DATETIME(6)  NOT NULL COMMENT '생성 일시',
    last_modified_at DATETIME(6)  NULL COMMENT '수정 일시',
    created_by       BIGINT       NOT NULL COMMENT '생성자 ID',
    last_modified_by BIGINT       NULL COMMENT '수정자 ID',

    course_id        BIGINT       NOT NULL COMMENT '커리큘럼이 속한 코스 ID',
    title            VARCHAR(255) NOT NULL COMMENT '커리큘럼 명',
    `description`    VARCHAR(255) NULL COMMENT '커리큘럼에 대한 간략한 설명',

    CONSTRAINT pk_curriculum PRIMARY KEY (id),
    -- 한 코스 내에서 커리큘럼 명은 중복될 수 없도록 UNIQUE 제약조건 추가
    -- 특정 코스에 속한 모든 커리큘럼 목록 조회 시나리오 >> 특정 제목을 가진 커리큘럼 검색 시나리오
    CONSTRAINT UK_curriculum_course_id_title UNIQUE (course_id, title),
    CONSTRAINT fk_curriculum_to_course FOREIGN KEY (course_id) REFERENCES course (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='스터디 과정에 속한 상세 커리큘럼을 관리하는 테이블';