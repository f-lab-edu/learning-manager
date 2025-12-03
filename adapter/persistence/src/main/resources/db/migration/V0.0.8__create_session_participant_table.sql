CREATE TABLE session_participant
(
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '세션 참여자 고유 ID',
    created_at       DATETIME(6)  NOT NULL COMMENT '생성 일시',
    last_modified_at DATETIME(6)  NULL COMMENT '수정 일시',
    created_by       BIGINT       NOT NULL COMMENT '생성자 ID',
    last_modified_by BIGINT       NULL COMMENT '수정자 ID',

    session_id       BIGINT       NOT NULL COMMENT '참여하는 세션 ID',
    member_id        BIGINT       NOT NULL COMMENT '참여하는 멤버 ID',
    `role`           VARCHAR(255) NOT NULL COMMENT '세션 내 역할 (예: HOST, SPEAKER, ATTENDEE)',

    CONSTRAINT pk_session_participant PRIMARY KEY (id),

    CONSTRAINT FK_session_participant_on_session FOREIGN KEY (session_id) REFERENCES session (id),
    CONSTRAINT FK_session_participant_on_member FOREIGN KEY (member_id) REFERENCES member (id),

    -- 한 멤버는 한 세션에 한 번만 참여할 수 있도록 UNIQUE 제약조건 추가
    CONSTRAINT UK_session_participant_session_member UNIQUE (session_id, member_id),

    -- 특정 멤버가 참여하는 모든 세션 조회를 위한 인덱스
    INDEX IX_session_participant_member_id (member_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='세션과 멤버의 참여 관계를 관리하는 테이블';
