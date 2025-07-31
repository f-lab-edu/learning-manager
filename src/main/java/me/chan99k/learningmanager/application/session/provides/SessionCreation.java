package me.chan99k.learningmanager.application.session.provides;

import me.chan99k.learningmanager.domain.session.Session;

/**
 * [P0] 스터디 세션 생성
 * 스터디장 또는 시스템 관리자가 새로운 스터디 세션을 생성하는 기능을 제공한다.
 */
public interface SessionCreation {
	Session createSession();
}
