-- ============================================================
-- Course Domain: course, curriculum, course_member
-- ============================================================

-- --------------------------------------
-- course: 스터디 과정
-- --------------------------------------
CREATE TABLE course
(
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '코스 고유 ID',
    created_at       DATETIME(6)  NOT NULL COMMENT '생성 일시',
    last_modified_at DATETIME(6)  NULL COMMENT '수정 일시',
    created_by       BIGINT       NOT NULL COMMENT '생성자 ID',
    last_modified_by BIGINT       NULL COMMENT '수정자 ID',
    version          BIGINT       NOT NULL DEFAULT 0 COMMENT '낙관적 락 버전',

    title            VARCHAR(255) NOT NULL COMMENT '코스명',
    description      VARCHAR(255) NULL COMMENT '코스에 대한 간략한 설명',

    CONSTRAINT pk_course PRIMARY KEY (id),
    CONSTRAINT uk_course_title UNIQUE (title)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT = '스터디 과정을 관리하는 테이블';

-- --------------------------------------
-- curriculum: 코스 내 커리큘럼
-- --------------------------------------
CREATE TABLE curriculum
(
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '커리큘럼 고유 ID',
    created_at       DATETIME(6)  NOT NULL COMMENT '생성 일시',
    last_modified_at DATETIME(6)  NULL COMMENT '수정 일시',
    created_by       BIGINT       NOT NULL COMMENT '생성자 ID',
    last_modified_by BIGINT       NULL COMMENT '수정자 ID',
    version          BIGINT       NOT NULL DEFAULT 0 COMMENT '낙관적 락 버전',

    course_id        BIGINT       NOT NULL COMMENT '커리큘럼이 속한 코스 ID',
    title            VARCHAR(255) NOT NULL COMMENT '커리큘럼 명',
    description      VARCHAR(255) NULL COMMENT '커리큘럼에 대한 간략한 설명',

    CONSTRAINT pk_curriculum PRIMARY KEY (id),
    CONSTRAINT uk_curriculum_course_title UNIQUE (course_id, title),
    CONSTRAINT fk_curriculum_course FOREIGN KEY (course_id) REFERENCES course (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT = '스터디 과정에 속한 상세 커리큘럼을 관리하는 테이블';

-- --------------------------------------
-- course_member: 코스-멤버 참여 관계
-- --------------------------------------
CREATE TABLE course_member
(
    id               BIGINT      NOT NULL AUTO_INCREMENT COMMENT '코스-멤버 관계 고유 ID',
    created_at       DATETIME(6) NOT NULL COMMENT '생성 일시',
    last_modified_at DATETIME(6) NULL COMMENT '수정 일시',
    created_by       BIGINT      NOT NULL COMMENT '생성자 ID',
    last_modified_by BIGINT      NULL COMMENT '수정자 ID',
    version          BIGINT      NOT NULL DEFAULT 0 COMMENT '낙관적 락 버전',

    course_id        BIGINT      NOT NULL COMMENT '참여하는 코스 ID',
    member_id        BIGINT      NOT NULL COMMENT '참여하는 멤버 ID',
    course_role      VARCHAR(20) NOT NULL COMMENT '코스 내 역할',

    CONSTRAINT pk_course_member PRIMARY KEY (id),
    CONSTRAINT uk_course_member UNIQUE (course_id, member_id),
    CONSTRAINT fk_course_member_course FOREIGN KEY (course_id) REFERENCES course (id),
    CONSTRAINT fk_course_member_member FOREIGN KEY (member_id) REFERENCES member (id),

    INDEX ix_course_member_member (member_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT = '스터디 과정과 멤버의 참여 관계를 관리하는 테이블';
