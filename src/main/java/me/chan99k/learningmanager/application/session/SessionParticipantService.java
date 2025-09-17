package me.chan99k.learningmanager.application.session;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.application.session.provides.SessionParticipantManagement;
import me.chan99k.learningmanager.application.session.requires.SessionCommandRepository;
import me.chan99k.learningmanager.application.session.requires.SessionQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.session.Session;
import me.chan99k.learningmanager.domain.session.SessionParticipantRole;
import me.chan99k.learningmanager.domain.session.SessionProblemCode;

@Service
@Transactional
public class SessionParticipantService implements SessionParticipantManagement {

	private final SessionQueryRepository sessionQueryRepository;
	private final SessionCommandRepository sessionCommandRepository;
	private final CourseQueryRepository courseQueryRepository;

	public SessionParticipantService(SessionQueryRepository sessionQueryRepository,
		SessionCommandRepository sessionCommandRepository, CourseQueryRepository courseQueryRepository) {
		this.sessionQueryRepository = sessionQueryRepository;
		this.sessionCommandRepository = sessionCommandRepository;
		this.courseQueryRepository = courseQueryRepository;
	}

	@Override
	public SessionParticipantResponse addParticipant(Long sessionId, AddParticipantRequest request) {
		Session session = getSessionById(sessionId);
		validateSessionParticipantManagementPermission(session);

		session.addParticipant(request.memberId(), request.role());
		Session savedSession = sessionCommandRepository.save(session);

		return toResponse(savedSession);
	}

	@Override
	public SessionParticipantResponse removeParticipant(RemoveParticipantRequest request) {
		Session session = getSessionById(request.sessionId());
		validateSessionParticipantManagementPermission(session);

		// HOST 자신을 제거하는 경우 검증
		validateHostSelfRemoval(session, request.memberId());

		session.removeParticipant(request.memberId());
		Session savedSession = sessionCommandRepository.save(session);

		return toResponse(savedSession);
	}

	@Override
	public SessionParticipantResponse changeParticipantRole(ChangeParticipantRoleRequest request) {
		Session session = getSessionById(request.sessionId());
		validateSessionParticipantManagementPermission(session);

		session.changeParticipantRole(request.memberId(), request.newRole());
		Session savedSession = sessionCommandRepository.save(session);

		return toResponse(savedSession);
	}

	private Session getSessionById(Long sessionId) {
		return sessionQueryRepository.findById(sessionId)
			.orElseThrow(() -> new DomainException(SessionProblemCode.SESSION_NOT_FOUND));
	}

	/**
	 * 현재 사용자가 세션 참여자 관리 권한이 있는지 확인합니다.
	 * 다음 중 하나의 조건을 만족해야 합니다:
	 * 1. 세션이 속한 Course의 MANAGER 역할을 가진 사용자
	 * 2. 해당 세션의 HOST 역할을 가진 참여자
	 *
	 * @param session 권한을 확인할 세션
	 * @throws AuthenticationException if authentication context is not found
	 * @throws AuthorizationException  if the member has no permission to manage session participants
	 */
	private void validateSessionParticipantManagementPermission(Session session) {
		Long currentMemberId = AuthenticationContextHolder.getCurrentMemberId()
			.orElseThrow(() -> new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND));

		// Course MANAGER 권한 확인 (세션이 Course에 속한 경우)
		boolean isCourseManager = false;
		if (session.getCourseId() != null) {
			isCourseManager = courseQueryRepository.findManagedCourseById(session.getCourseId(), currentMemberId)
				.isPresent();
		}

		// 세션 HOST 권한 확인
		boolean isSessionHost = session.getParticipants().stream()
			.anyMatch(p -> p.getMemberId().equals(currentMemberId)
				&& p.getRole() == SessionParticipantRole.HOST);

		// 둘 중 하나 만족시 권한 허용
		if (!isCourseManager && !isSessionHost) {
			throw new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED);
		}
	}

	/**
	 * HOST가 자신을 세션에서 제거하려고 할 때의 검증 로직입니다.
	 * HOST는 다른 HOST가 있는 경우에만 자신을 제거할 수 있습니다.
	 *
	 * @param session 세션 정보
	 * @param memberId 제거하려는 멤버 ID
	 * @throws AuthorizationException HOST가 혼자 남은 상태에서 자신을 제거하려고 할 때
	 */
	private void validateHostSelfRemoval(Session session, Long memberId) {
		Long currentMemberId = AuthenticationContextHolder.getCurrentMemberId()
			.orElseThrow(() -> new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND));

		// 현재 사용자가 제거하려는 대상이 아닌 경우 검증 불필요
		if (!currentMemberId.equals(memberId)) {
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
	public SessionParticipantResponse leaveSession(LeaveSessionRequest request) {
		Session session = getSessionById(request.sessionId());

		// 루트 세션에서는 자가 탈퇴 불가능
		if (session.isRootSession()) {
			throw new DomainException(SessionProblemCode.ROOT_SESSION_SELF_LEAVE_NOT_ALLOWED);
		}

		Long currentMemberId = AuthenticationContextHolder.getCurrentMemberId()
			.orElseThrow(() -> new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND));

		// HOST 자신을 제거하는 경우 검증
		validateHostSelfRemoval(session, currentMemberId);

		session.removeParticipant(currentMemberId);
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
