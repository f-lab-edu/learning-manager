INSERT INTO member (id, created_at, last_modified_at, created_by, last_modified_by, nickname, role, status,
                    profile_image_url, self_introduction)
VALUES (1, NOW(6), NOW(6), 1, 1,
        'SYSTEM', 'ADMIN', 'ACTIVE',
        NULL, '시스템 계정');

ALTER TABLE member
    AUTO_INCREMENT = 2;

INSERT INTO account (created_at, last_modified_at, created_by, last_modified_by, member_id, status, email, password)
VALUES (NOW(6), NOW(6), 1, 1, 1,
        'ACTIVE',
        'system@learning.manager',
        'SYSTEM_ACCOUNT_NO_LOGIN_POSSIBLE' -- 로그인이 불가능한 명시적 값 (인코더가 만들어 낼수 없음. 아마도...)
       );

ALTER TABLE account
    AUTO_INCREMENT = 2;