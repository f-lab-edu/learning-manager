package me.chan99k.learningmanager.application.session.provides;

import me.chan99k.learningmanager.domain.session.Session;

/**
 * [P2] 스터디 세션 참여자 관리
 */
public interface SessionParticipantManagement {

	Session addParticipant();

	Session removeParticipant();

	Session changeParticipantRole();
}
