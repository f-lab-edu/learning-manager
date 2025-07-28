CREATE TABLE session
(
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '세션 고유 ID',
    created_at       DATETIME(6)  NOT NULL COMMENT '생성 일시',
    last_modified_at DATETIME(6)  NULL COMMENT '수정 일시',
    created_by       BIGINT       NOT NULL COMMENT '생성자 ID',
    last_modified_by BIGINT       NULL COMMENT '수정자 ID',

    course_id        BIGINT       NULL COMMENT '세션이 속한 코스 ID',
    curriculum_id    BIGINT       NULL COMMENT '세션이 속한 커리큘럼 ID',
    parent_id        BIGINT       NULL COMMENT '부모 세션 ID (하위 세션인 경우)',

    title            VARCHAR(255) NOT NULL COMMENT '세션명',
    scheduled_at     DATETIME(6)  NOT NULL COMMENT '세션 시작 예정 시간',
    scheduled_end_at DATETIME(6)  NOT NULL COMMENT '세션 종료 예정 시간',
    type             VARCHAR(50)  NOT NULL COMMENT '세션 타입 (ONLINE, OFFLINE)',
    location         VARCHAR(50)  NOT NULL COMMENT '세션 장소 타입 (GOOGLE_MEET, ZOOM, SITE)',
    location_details VARCHAR(255) NULL COMMENT '오프라인 세션의 경우 상세 장소',

    CONSTRAINT pk_session PRIMARY KEY (id),

    CONSTRAINT FK_session_on_course FOREIGN KEY (course_id) REFERENCES course (id),
    CONSTRAINT FK_session_on_curriculum FOREIGN KEY (curriculum_id) REFERENCES curriculum (id),
    CONSTRAINT FK_session_on_parent FOREIGN KEY (parent_id) REFERENCES session (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='스터디 세션(일일 정규 출석, 멘토링 등) 정보를 관리하는 테이블';