package me.chan99k.learningmanager.authorization;

import org.springframework.stereotype.Service;

import me.chan99k.learningmanager.session.SessionQueryRepository;

@Service("sessionSecurity")
public class SessionSecurityService {
	private final SessionAuthorizationPort sessionAuthorizationPort;
	private final SessionQueryRepository sessionQueryRepository;

	public SessionSecurityService(SessionAuthorizationPort sessionAuthorizationPort,
		SessionQueryRepository sessionQueryRepository) {
		this.sessionAuthorizationPort = sessionAuthorizationPort;
		this.sessionQueryRepository = sessionQueryRepository;
	}

	public boolean isSessionManager(Long sessionId, Long memberId) {
		// TODO(human): 세션→과정 조회 후 MANAGER 권한 확인
		return false;
	}

	public boolean isSessionManagerOrMentor(Long sessionId, Long memberId) {
		// TODO(human): 세션→과정 조회 후 MANAGER/MENTOR 권한 확인
		return false;
	}

	public boolean isSessionMember(Long sessionId, Long memberId) {
		// TODO(human): 세션→과정 조회 후 멤버 여부 확인
		return false;
	}
}
