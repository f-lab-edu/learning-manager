# member
alter table `member`
    add column `version` BIGINT NOT NULL DEFAULT 0;
alter table `account`
    add column `version` BIGINT NOT NULL DEFAULT 0;

# course
alter table `course`
    add column `version` BIGINT NOT NULL DEFAULT 0;
alter table `course_member`
    add column `version` BIGINT NOT NULL DEFAULT 0;
alter table `curriculum`
    add column `version` BIGINT NOT NULL DEFAULT 0;

# session
alter table `session`
    add column `version` BIGINT NOT NULL DEFAULT 0;
alter table `session_participant`
    add column `version` BIGINT NOT NULL DEFAULT 0;

# attendance
alter table `attendance`
    add column `version` BIGINT NOT NULL DEFAULT 0;
