package me.chan99k.learningmanager.session;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.exception.DomainException;

@Service
@Transactional
public class SessionParticipantService implements SessionParticipantManagement {

	private final SessionQueryRepository sessionQueryRepository;
	private final SessionCommandRepository sessionCommandRepository;
	private final Clock clock;

	public SessionParticipantService(SessionQueryRepository sessionQueryRepository,
		SessionCommandRepository sessionCommandRepository,
		Clock clock) {
		this.sessionQueryRepository = sessionQueryRepository;
		this.sessionCommandRepository = sessionCommandRepository;

		this.clock = clock;
	}

	@Override
	public SessionParticipantResponse addParticipant(Long requestedBy, Long sessionId, AddParticipantRequest request) {
		Session session = getSessionById(sessionId);

		session.addParticipant(request.memberId(), request.role());
		Session savedSession = sessionCommandRepository.save(session);

		return toResponse(savedSession);
	}

	@Override
	public SessionParticipantResponse removeParticipant(Long requestedBy, RemoveParticipantRequest request) {
		Session session = getSessionById(request.sessionId());

		// HOST 자신을 제거하는 경우 검증
		validateHostSelfRemoval(session, request.memberId(), requestedBy);

		session.removeParticipant(request.memberId());
		Session savedSession = sessionCommandRepository.save(session);

		return toResponse(savedSession);
	}

	@Override
	public SessionParticipantResponse changeParticipantRole(Long requestedBy, ChangeParticipantRoleRequest request) {
		Session session = getSessionById(request.sessionId());

		session.changeParticipantRole(request.memberId(), request.newRole(), clock);
		Session savedSession = sessionCommandRepository.save(session);

		return toResponse(savedSession);
	}

	private Session getSessionById(Long sessionId) {
		return sessionQueryRepository.findById(sessionId)
			.orElseThrow(() -> new DomainException(SessionProblemCode.SESSION_NOT_FOUND));
	}

	/**
	 * HOST가 자신을 세션에서 제거하려고 할 때의 검증 로직입니다.
	 * HOST는 다른 HOST가 있는 경우에만 자신을 제거할 수 있습니다.
	 *
	 * @param session     세션 정보
	 * @param memberId    제거하려는 멤버 ID
	 * @param requestedBy 요청자 ID
	 * @throws DomainException HOST가 혼자 남은 상태에서 자신을 제거하려고 할 때
	 */
	private void validateHostSelfRemoval(Session session, Long memberId, Long requestedBy) {
		// 현재 사용자가 제거하려는 대상이 아닌 경우 검증 불필요
		if (!requestedBy.equals(memberId)) {
			return;
		}

		// 제거하려는 사용자가 HOST인지 확인
		boolean isRemovingHost = session.getParticipants().stream()
			.anyMatch(p -> p.getMemberId().equals(memberId) && p.getRole() == SessionParticipantRole.HOST);

		if (isRemovingHost) {
			// 다른 HOST가 있는지 확인
			long hostCount = session.getParticipants().stream()
				.filter(p -> p.getRole() == SessionParticipantRole.HOST)
				.count();

			if (hostCount <= 1) {
				throw new DomainException(SessionProblemCode.HOST_CANNOT_LEAVE_ALONE);
			}
		}
	}

	@Override
	public SessionParticipantResponse leaveSession(Long requestedBy, LeaveSessionRequest request) {
		Session session = getSessionById(request.sessionId());

		// 루트 세션에서는 자가 탈퇴 불가능
		if (session.isRootSession()) {
			throw new DomainException(SessionProblemCode.ROOT_SESSION_SELF_LEAVE_NOT_ALLOWED);
		}

		// HOST 자신을 제거하는 경우 검증
		validateHostSelfRemoval(session, requestedBy, requestedBy);

		session.removeParticipant(requestedBy);
		Session savedSession = sessionCommandRepository.save(session);

		return toResponse(savedSession);
	}

	private SessionParticipantResponse toResponse(Session session) {
		var participants = session.getParticipants().stream()
			.map(p -> new ParticipantInfo(p.getMemberId(), p.getRole()))
			.toList();

		return new SessionParticipantResponse(
			session.getId(),
			session.getTitle(),
			participants
		);
	}
}
