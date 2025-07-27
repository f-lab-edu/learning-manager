CREATE TABLE course_member
(
    id               BIGINT      NOT NULL AUTO_INCREMENT COMMENT '코스-멤버 관계 고유 ID',
    created_at       DATETIME(6) NOT NULL COMMENT '생성 일시',
    last_modified_at DATETIME(6) NULL COMMENT '수정 일시',
    created_by       BIGINT      NOT NULL COMMENT '생성자 ID',
    last_modified_by BIGINT      NULL COMMENT '수정자 ID',

    course_id        BIGINT      NOT NULL COMMENT '참여하는 코스 ID',
    member_id        BIGINT      NOT NULL COMMENT '참여하는 멤버 ID',
    course_role      VARCHAR(20) NOT NULL COMMENT '코스 내 역할',

    CONSTRAINT pk_coursemember PRIMARY KEY (id),
    CONSTRAINT FK_coursemember_on_course FOREIGN KEY (course_id) REFERENCES course (id),
    CONSTRAINT FK_coursemember_on_member FOREIGN KEY (member_id) REFERENCES member (id),
    -- 한 멤버는 한 코스에 하나의 역할만 가질 수 있도록 UNIQUE 제약조건 추가 (중복 역할 방지)
    CONSTRAINT UK_course_member_course_id_member_id UNIQUE (course_id, member_id),
    -- 특정 멤버가 참여하는 모든 과정 조회 시나리오를 위한 인덱스
    INDEX IX_course_member_member_id (member_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='스터디 과정과 멤버의 참여 관계를 관리하는 테이블';