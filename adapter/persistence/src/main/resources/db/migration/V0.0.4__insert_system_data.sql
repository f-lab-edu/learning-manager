-- ============================================================
-- System Initial Data
-- ============================================================

-- 시스템 계정 생성
INSERT INTO member (id, created_at, last_modified_at, created_by, last_modified_by,
                    version, nickname, role, status, primary_email,
                    profile_image_url, self_introduction)
VALUES (1, NOW(6), NOW(6), 1, 1,
        0, 'SYSTEM', 'ADMIN', 'ACTIVE', 'system@learning.manager',
        NULL, '시스템 계정');

ALTER TABLE member
    AUTO_INCREMENT = 2;

INSERT INTO account (created_at, last_modified_at, created_by, last_modified_by,
                     version, member_id, status, email)
VALUES (NOW(6), NOW(6), 1, 1,
        0, 1, 'ACTIVE', 'system@learning.manager');

ALTER TABLE account
    AUTO_INCREMENT = 2;

-- 시스템 계정은 로그인 불가능하도록 credential 없음
