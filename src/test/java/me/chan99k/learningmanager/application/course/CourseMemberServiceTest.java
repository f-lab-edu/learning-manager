package me.chan99k.learningmanager.application.course;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.application.course.provides.CourseMemberAddition;
import me.chan99k.learningmanager.application.course.requires.CourseCommandRepository;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.application.member.requires.MemberEmailPair;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.course.CourseMember;
import me.chan99k.learningmanager.domain.course.CourseProblemCode;
import me.chan99k.learningmanager.domain.course.CourseRole;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;

@ExtendWith(MockitoExtension.class)
class CourseMemberServiceTest {

	private final Long courseId = 1L;
	private final Long managerId = 10L;
	private final Long memberToAddId = 20L;
	private final String memberToAddEmail = "add@example.com";

	private CourseMemberService courseMemberService;
	@Mock
	private CourseQueryRepository courseQueryRepository;
	@Mock
	private CourseCommandRepository courseCommandRepository;
	@Mock
	private MemberQueryRepository memberQueryRepository;
	@Mock
	private Course course;
	@Mock
	private Member manager;
	@Mock
	private Member memberToAdd;
	@Mock
	private CourseMember courseMember;

	@BeforeEach
	void setUp() {
		courseMemberService = new CourseMemberService(
			100, // test용 bulk size 직접 지정
			courseQueryRepository,
			courseCommandRepository,
			memberQueryRepository
		);
		lenient().when(memberToAdd.getId()).thenReturn(memberToAddId);
	}

	private CourseMemberAddition.Request createRequest(String email, CourseRole role) {
		return new CourseMemberAddition.Request(List.of(
			new CourseMemberAddition.MemberAdditionItem(email, role)
		));
	}

	@Test
	@DisplayName("[Success] 과정 매니저가 새로운 멤버를 성공적으로 추가한다")
	void addSingleMember_Success() {
		CourseMemberAddition.MemberAdditionItem item = new CourseMemberAddition.MemberAdditionItem(memberToAddEmail,
			CourseRole.MENTEE);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
			when(memberQueryRepository.findByEmail(Email.of(memberToAddEmail))).thenReturn(Optional.of(memberToAdd));

			courseMemberService.addSingleMember(courseId, item);

			verify(course).addMember(memberToAddId, CourseRole.MENTEE);
			verify(courseCommandRepository).save(course);
		}
	}

	@Test
	@DisplayName("[Failure] 단일 요청에서 인증된 사용자 정보가 없으면 AuthException이 발생한다")
	void addSingleMember_Fail_Unauthenticated() {
		CourseMemberAddition.MemberAdditionItem item = new CourseMemberAddition.MemberAdditionItem(memberToAddEmail,
			CourseRole.MENTEE);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.empty());

			assertThatThrownBy(() -> courseMemberService.addSingleMember(courseId, item))
				.isInstanceOf(AuthenticationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND);
		}
	}

	@Test
	@DisplayName("[Failure] 단일 요청에서 과정이 존재하지 않거나 매니저가 아니면 AuthorizationException이 발생한다")
	void addSingleMember_Fail_CourseNotFoundOrNotManager() {
		CourseMemberAddition.MemberAdditionItem item = new CourseMemberAddition.MemberAdditionItem(memberToAddEmail,
			CourseRole.MENTEE);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			// findManagedCourseById가 Optional.empty()를 반환하는 경우 (코스가 없거나, 매니저가 아님)
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> courseMemberService.addSingleMember(courseId, item))
				.isInstanceOf(AuthorizationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);
		}
	}

	@Test
	@DisplayName("[Failure] 단일 요청에서 추가하려는 멤버가 존재하지 않으면 DomainException이 발생한다")
	void addSingleMember_Fail_MemberNotFound() {
		// given
		CourseMemberAddition.MemberAdditionItem item = new CourseMemberAddition.MemberAdditionItem(memberToAddEmail,
			CourseRole.MENTEE);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
			when(memberQueryRepository.findByEmail(Email.of(memberToAddEmail))).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> courseMemberService.addSingleMember(courseId, item))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.MEMBER_NOT_FOUND);
		}
	}

	@Test
	@DisplayName("[Failure] 벌크 요청에서 일부 멤버가 존재하지 않으면 실패 정보가 반환된다")
	void addMultipleMembers_PartialSuccess_MemberNotFound() {
		List<CourseMemberAddition.MemberAdditionItem> members = List.of(
			new CourseMemberAddition.MemberAdditionItem("success@example.com", CourseRole.MENTEE),
			new CourseMemberAddition.MemberAdditionItem("notfound@example.com", CourseRole.MENTEE)
		);

		// 성공할 멤버만 반환되도록 Mock 설정
		MemberEmailPair successPair = new MemberEmailPair(memberToAdd, "success@example.com");
		List<MemberEmailPair> foundPairs = List.of(successPair);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
			when(memberQueryRepository.findMembersByEmails(anyList(), eq(100))).thenReturn(foundPairs);

			CourseMemberAddition.Response response = courseMemberService.addMultipleMembers(courseId, members);

			assertThat(response.totalCount()).isEqualTo(2);
			assertThat(response.successCount()).isEqualTo(1);
			assertThat(response.failureCount()).isEqualTo(1);
			assertThat(response.results()).hasSize(2);

			// 성공 결과 확인
			assertThat(response.results().get(0).email()).isEqualTo("success@example.com");
			assertThat(response.results().get(0).status()).isEqualTo("SUCCESS");

			// 실패 결과 확인
			assertThat(response.results().get(1).email()).isEqualTo("notfound@example.com");
			assertThat(response.results().get(1).status()).isEqualTo("FAILED");
			assertThat(response.results().get(1).message()).contains("해당 회원이 존재하지 않습니다");

			verify(courseCommandRepository).save(course);
		}
	}

	@Test
	@DisplayName("[Failure] 벌크 요청에서 도메인 규칙 위반 시 실패 정보가 반환된다")
	void addMultipleMembers_Fail_DomainRuleViolation() {
		List<CourseMemberAddition.MemberAdditionItem> members = List.of(
			new CourseMemberAddition.MemberAdditionItem(memberToAddEmail, CourseRole.MENTEE)
		);
		MemberEmailPair memberPair = new MemberEmailPair(memberToAdd, memberToAddEmail);
		doThrow(new IllegalArgumentException(CourseProblemCode.COURSE_MEMBER_ALREADY_REGISTERED.getMessage()))
			.when(course).addMember(anyLong(), any(CourseRole.class));

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
			when(memberQueryRepository.findMembersByEmails(anyList(), eq(100))).thenReturn(List.of(memberPair));

			CourseMemberAddition.Response response = courseMemberService.addMultipleMembers(courseId, members);

			assertThat(response.totalCount()).isEqualTo(1);
			assertThat(response.successCount()).isEqualTo(0);
			assertThat(response.failureCount()).isEqualTo(1);
			assertThat(response.results().get(0).status()).isEqualTo("FAILED");
			assertThat(response.results().get(0).message()).contains("이미 과정에 등록된 멤버");

			verify(courseCommandRepository).save(course);
		}
	}

	@Test
	@DisplayName("[Success] 과정 매니저가 멤버를 성공적으로 제외한다")
	void removeMember_Success() {
		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			// given
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

			// when
			courseMemberService.removeMemberFromCourse(courseId, memberToAddId);

			// then
			verify(course).removeMember(memberToAddId);
			verify(courseCommandRepository).save(course);
		}
	}

	@Test
	@DisplayName("[Failure] 과정 매니저가 아니면 멤버를 제외할 수 없다")
	void removeMember_Fail_NotManager() {
		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			// given
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> courseMemberService.removeMemberFromCourse(courseId, memberToAddId))
				.isInstanceOf(AuthorizationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);

			verify(course, never()).removeMember(anyLong());
			verify(courseCommandRepository, never()).save(any(Course.class));
		}
	}
}