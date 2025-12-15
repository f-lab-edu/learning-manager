package me.chan99k.learningmanager.course;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.Email;
import me.chan99k.learningmanager.member.Member;
import me.chan99k.learningmanager.member.MemberEmailPair;
import me.chan99k.learningmanager.member.MemberProblemCode;
import me.chan99k.learningmanager.member.MemberQueryRepository;

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
	private Member memberToAdd;

	@BeforeEach
	void setUp() {
		courseMemberService = new CourseMemberService(
			100,
			courseQueryRepository,
			courseCommandRepository,
			memberQueryRepository
		);
		lenient().when(memberToAdd.getId()).thenReturn(memberToAddId);
	}

	@Test
	@DisplayName("[Success] 과정 매니저가 새로운 멤버를 성공적으로 추가한다")
	void addSingleMember_Success() {
		CourseMemberAddition.MemberAdditionItem item = new CourseMemberAddition.MemberAdditionItem(memberToAddEmail,
			CourseRole.MENTEE);

		given(courseQueryRepository.findManagedCourseById(courseId, managerId)).willReturn(Optional.of(course));
		given(memberQueryRepository.findByEmail(Email.of(memberToAddEmail))).willReturn(Optional.of(memberToAdd));
		given(memberToAdd.getId()).willReturn(memberToAddId);

		assertThatCode(() -> courseMemberService.addSingleMember(managerId, courseId, item))
			.doesNotThrowAnyException();

		then(course).should().addMember(memberToAddId, CourseRole.MENTEE);
		then(courseCommandRepository).should().save(course);
	}

	@Test
	@DisplayName("[Failure] 단일 요청에서 과정이 존재하지 않거나 매니저가 아니면 DomainException이 발생한다")
	void addSingleMember_Fail_CourseNotFoundOrNotManager() {
		CourseMemberAddition.MemberAdditionItem item = new CourseMemberAddition.MemberAdditionItem(memberToAddEmail,
			CourseRole.MENTEE);

		given(courseQueryRepository.findManagedCourseById(courseId, managerId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> courseMemberService.addSingleMember(managerId, courseId, item))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", CourseProblemCode.NOT_COURSE_MANAGER);

		then(memberQueryRepository).shouldHaveNoInteractions();
		then(courseCommandRepository).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("[Failure] 단일 요청에서 추가하려는 멤버가 존재하지 않으면 DomainException이 발생한다")
	void addSingleMember_Fail_MemberNotFound() {
		CourseMemberAddition.MemberAdditionItem item = new CourseMemberAddition.MemberAdditionItem(memberToAddEmail,
			CourseRole.MENTEE);

		given(courseQueryRepository.findManagedCourseById(courseId, managerId)).willReturn(Optional.of(course));
		given(memberQueryRepository.findByEmail(Email.of(memberToAddEmail))).willReturn(Optional.empty());

		assertThatThrownBy(() -> courseMemberService.addSingleMember(managerId, courseId, item))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.MEMBER_NOT_FOUND);

		then(courseCommandRepository).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("[Failure] 벌크 요청에서 일부 멤버가 존재하지 않으면 실패 정보가 반환된다")
	void addMultipleMembers_PartialSuccess_MemberNotFound() {
		List<CourseMemberAddition.MemberAdditionItem> members = List.of(
			new CourseMemberAddition.MemberAdditionItem("success@example.com", CourseRole.MENTEE),
			new CourseMemberAddition.MemberAdditionItem("notfound@example.com", CourseRole.MENTEE)
		);

		MemberEmailPair successPair = new MemberEmailPair(memberToAdd, "success@example.com");
		List<MemberEmailPair> foundPairs = List.of(successPair);

		given(courseQueryRepository.findManagedCourseById(courseId, managerId)).willReturn(Optional.of(course));
		given(memberQueryRepository.findMembersByEmails(any(), eq(100))).willReturn(foundPairs);
		given(memberToAdd.getId()).willReturn(memberToAddId);

		CourseMemberAddition.Response response = courseMemberService.addMultipleMembers(managerId, courseId, members);

		assertThat(response.totalCount()).isEqualTo(2);
		assertThat(response.successCount()).isEqualTo(1);
		assertThat(response.failureCount()).isEqualTo(1);
		assertThat(response.results()).hasSize(2);
		assertThat(response.results().get(0).status()).isEqualTo("SUCCESS");
		assertThat(response.results().get(1).status()).isEqualTo("FAILED");
		assertThat(response.results().get(1).message()).isEqualTo("해당 회원이 존재하지 않습니다");

		then(course).should().addMember(memberToAddId, CourseRole.MENTEE);
		then(courseCommandRepository).should().save(course);
	}

	@Test
	@DisplayName("[Failure] 벌크 요청에서 도메인 규칙 위반 시 실패 정보가 반환된다")
	void addMultipleMembers_Fail_DomainRuleViolation() {
		List<CourseMemberAddition.MemberAdditionItem> members = List.of(
			new CourseMemberAddition.MemberAdditionItem(memberToAddEmail, CourseRole.MENTEE)
		);

		MemberEmailPair memberPair = new MemberEmailPair(memberToAdd, memberToAddEmail);
		List<MemberEmailPair> foundPairs = List.of(memberPair);

		given(courseQueryRepository.findManagedCourseById(courseId, managerId)).willReturn(Optional.of(course));
		given(memberQueryRepository.findMembersByEmails(any(), eq(100))).willReturn(foundPairs);
		given(memberToAdd.getId()).willReturn(memberToAddId);
		doThrow(new IllegalArgumentException("이미 과정에 참여 중인 회원입니다"))
			.when(course).addMember(memberToAddId, CourseRole.MENTEE);

		CourseMemberAddition.Response response = courseMemberService.addMultipleMembers(managerId, courseId, members);

		assertThat(response.totalCount()).isEqualTo(1);
		assertThat(response.successCount()).isZero();
		assertThat(response.failureCount()).isEqualTo(1);
		assertThat(response.results()).hasSize(1);
		assertThat(response.results().get(0).status()).isEqualTo("FAILED");
		assertThat(response.results().get(0).message()).isEqualTo("이미 과정에 참여 중인 회원입니다");

		then(courseCommandRepository).should().save(course);
	}

	@Test
	@DisplayName("[Success] 과정 매니저가 멤버를 성공적으로 제외한다")
	void removeMember_Success() {
		given(courseQueryRepository.findManagedCourseById(courseId, managerId)).willReturn(Optional.of(course));

		assertThatCode(() -> courseMemberService.removeMemberFromCourse(managerId, courseId, memberToAddId))
			.doesNotThrowAnyException();

		then(course).should().removeMember(memberToAddId);
		then(courseCommandRepository).should().save(course);
	}

	@Test
	@DisplayName("[Failure] 과정 매니저가 아니면 멤버를 제외할 수 없다")
	void removeMember_Fail_NotManager() {
		given(courseQueryRepository.findManagedCourseById(courseId, managerId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> courseMemberService.removeMemberFromCourse(managerId, courseId, memberToAddId))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", CourseProblemCode.NOT_COURSE_MANAGER);

		then(courseCommandRepository).shouldHaveNoInteractions();
	}
}
