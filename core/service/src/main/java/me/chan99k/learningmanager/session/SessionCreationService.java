package me.chan99k.learningmanager.session;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.auth.UserContext;
import me.chan99k.learningmanager.course.Course;
import me.chan99k.learningmanager.course.CourseProblemCode;
import me.chan99k.learningmanager.course.CourseQueryRepository;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.Member;
import me.chan99k.learningmanager.member.MemberProblemCode;
import me.chan99k.learningmanager.member.MemberQueryRepository;
import me.chan99k.learningmanager.member.SystemRole;

@Service
@Transactional
public class SessionCreationService implements SessionCreation {
	private final SessionQueryRepository sessionQueryRepository;
	private final SessionCommandRepository sessionCommandRepository;
	private final CourseQueryRepository courseQueryRepository;
	private final MemberQueryRepository memberQueryRepository;
	private final Clock clock;
	private final UserContext userContext;

	public SessionCreationService(SessionQueryRepository sessionQueryRepository,
		SessionCommandRepository sessionCommandRepository,
		CourseQueryRepository courseQueryRepository,
		MemberQueryRepository memberQueryRepository,
		Clock clock,
		UserContext userContext) {
		this.sessionQueryRepository = sessionQueryRepository;
		this.sessionCommandRepository = sessionCommandRepository;
		this.courseQueryRepository = courseQueryRepository;
		this.memberQueryRepository = memberQueryRepository;
		this.clock = clock;
		this.userContext = userContext;
	}

	@Override
	public Session createSession(Request request) {
		Long currentMemberId = getCurrentMemberId();
		validatePermission(request, currentMemberId);

		Session session = createSessionByType(request);
		return sessionCommandRepository.create(session);
	}

	private Long getCurrentMemberId() {
		return userContext.getCurrentMemberId();
	}

	private void validatePermission(Request request, Long memberId) {
		Member member = memberQueryRepository.findById(memberId)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		// 단독 세션은 시스템 관리자만 생성 가능
		if (isStandaloneSession(request)) {
			if (!member.getRole().equals(SystemRole.ADMIN)) {
				throw new DomainException(MemberProblemCode.ADMIN_ONLY_ACTION);
			}
			return;
		}

		// 과정/커리큘럼 세션은 해당 과정의 매니저만 생성 가능
		if (request.courseId() != null) {
			Course course = courseQueryRepository.findManagedCourseById(request.courseId(), memberId)
				.orElseThrow(() -> new DomainException(CourseProblemCode.NOT_COURSE_MANAGER));

			// 커리큘럼 세션인 경우 커리큘럼 존재 여부 확인
			if (request.curriculumId() != null) {
				boolean curriculumExists = course.getCurriculumList().stream()
					.anyMatch(curriculum -> curriculum.getId().equals(request.curriculumId()));
				if (!curriculumExists) {
					throw new DomainException(CourseProblemCode.CURRICULUM_NOT_FOUND_IN_COURSE);
				}
			}
		}
	}

	private Session createSessionByType(Request request) {
		if (request.sessionId() != null) {
			// 하위 세션 생성
			Session parent = sessionQueryRepository.findById(request.sessionId())
				.orElseThrow(() -> new DomainException(SessionProblemCode.SESSION_NOT_FOUND));

			return parent.createChildSession(
				request.title(), request.scheduledAt(), request.scheduledEndAt(),
				request.type(), request.location(), request.locationDetails(), clock
			);
		} else if (request.curriculumId() != null) {
			// 커리큘럼 세션 생성
			return Session.createCurriculumSession(
				request.courseId(), request.curriculumId(),
				request.title(), request.scheduledAt(), request.scheduledEndAt(),
				request.type(), request.location(), request.locationDetails(), clock
			);
		} else if (request.courseId() != null) {
			// 과정 세션 생성
			return Session.createCourseSession(
				request.courseId(),
				request.title(), request.scheduledAt(), request.scheduledEndAt(),
				request.type(), request.location(), request.locationDetails(), clock
			);
		} else {
			// 단독 세션 생성
			return Session.createStandaloneSession(
				request.title(), request.scheduledAt(), request.scheduledEndAt(),
				request.type(), request.location(), request.locationDetails(), clock
			);
		}
	}

	private boolean isStandaloneSession(Request request) {
		return request.courseId() == null && request.curriculumId() == null && request.sessionId() == null;
	}
}
