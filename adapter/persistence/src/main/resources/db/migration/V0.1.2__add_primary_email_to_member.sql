-- member 테이블에 primary_email 컬럼 추가
ALTER TABLE member
    ADD COLUMN primary_email VARCHAR(255) NOT NULL DEFAULT '' COMMENT '대표 이메일 주소';

-- 기존 데이터: 각 멤버의 첫 번째 account 이메일을 primary_email로 설정
UPDATE member m
SET m.primary_email = (SELECT a.email
                       FROM account a
                       WHERE a.member_id = m.id
                       ORDER BY a.id
                       LIMIT 1)
WHERE EXISTS (SELECT 1
              FROM account a
              WHERE a.member_id = m.id);

-- DEFAULT 제약 제거 (NOT NULL만 유지)
ALTER TABLE member
    ALTER COLUMN primary_email DROP DEFAULT;