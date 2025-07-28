CREATE TABLE attendance
(
    id               BIGINT      NOT NULL AUTO_INCREMENT COMMENT '출석 고유 ID',
    created_at       DATETIME(6) NOT NULL COMMENT '생성 일시',
    last_modified_at DATETIME(6) NULL COMMENT '수정 일시',
    created_by       BIGINT      NOT NULL COMMENT '생성자 ID',
    last_modified_by BIGINT      NULL COMMENT '수정자 ID',

    session_id       BIGINT      NOT NULL COMMENT '스터디 세션 ID',
    member_id        BIGINT      NOT NULL COMMENT '참석한 멤버 ID',
    check_in_time    DATETIME(6) NOT NULL COMMENT '입실 시간',
    check_out_time   DATETIME(6) NULL COMMENT '퇴실 시간',

    CONSTRAINT pk_attendance PRIMARY KEY (id),
    CONSTRAINT FK_attendance_on_session FOREIGN KEY (session_id) REFERENCES session (id),
    CONSTRAINT FK_attendance_on_member FOREIGN KEY (member_id) REFERENCES member (id),
    -- 한 멤버는 한 세션에 한 번만 출석할 수 있도록 UNIQUE 제약 조건 추가
    -- 특정 멤버의 모든 출석 기록 조회 시나리오 << 특정 세션에 참석한 모든 멤버 목록 조회 시나리오
    CONSTRAINT UK_attendance_session_member UNIQUE (session_id, member_id),
    -- 특정 멤버의 모든 출석 기록 조회 시나리오 를 위한 인덱스
    INDEX IX_attendance_member_id (member_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='세션 출석 정보를 관리하는 테이블';
