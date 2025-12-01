package me.chan99k.learningmanager.application.course;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.application.auth.UserContext;
import me.chan99k.learningmanager.application.course.provides.CourseMemberAddition;
import me.chan99k.learningmanager.application.course.provides.CourseMemberRemoval;
import me.chan99k.learningmanager.application.member.MemberEmailPair;
import me.chan99k.learningmanager.application.member.MemberQueryRepository;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.exception.AuthProblemCode;
import me.chan99k.learningmanager.domain.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.exception.DomainException;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;

@Service
@Transactional
public class CourseMemberService implements CourseMemberAddition, CourseMemberRemoval {

	private final int MAX_BULK_SIZE;

	private final CourseQueryRepository queryRepository;
	private final CourseCommandRepository commandRepository;
	private final MemberQueryRepository memberQueryRepository;
	private final UserContext userContext;

	public CourseMemberService(
		@Value("${course.member.bulk.max-size}")
		int maxBulkSize,
		CourseQueryRepository queryRepository,
		CourseCommandRepository commandRepository, MemberQueryRepository memberQueryRepository,
		UserContext userContext) {
		MAX_BULK_SIZE = maxBulkSize;
		this.queryRepository = queryRepository;
		this.commandRepository = commandRepository;
		this.memberQueryRepository = memberQueryRepository;
		this.userContext = userContext;
	}

	@Override
	public void addSingleMember(Long courseId, MemberAdditionItem item) {
		Course course = queryRepository.findManagedCourseById(courseId, userContext.getCurrentMemberId())
			.orElseThrow(() -> new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));

		Member member = memberQueryRepository.findByEmail(Email.of(item.email()))
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		course.addMember(member.getId(), item.role());

		commandRepository.save(course);
	}

	/**
	 * 벌크 멤버 추가 로직 + 각 멤버별 상세 결과 수집 , 예외 잡아서 MemberResult로 변환
	 */
	@Override
	public CourseMemberAddition.Response addMultipleMembers(Long courseId, List<MemberAdditionItem> members) {
		if (members.size() > MAX_BULK_SIZE) {
			throw new IllegalArgumentException("과정 멤버 추가 요청은 한번에 최대 " + MAX_BULK_SIZE + "개까지 가능합니다");
		}

		Course course = queryRepository.findManagedCourseById(courseId, userContext.getCurrentMemberId())
			.orElseThrow(() -> new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));

		// 멤버 조회 로직
		List<Email> emails = members.stream().map(item -> Email.of(item.email())).toList();
		List<MemberEmailPair> memberPairs = memberQueryRepository.findMembersByEmails(emails, MAX_BULK_SIZE);
		Map<String, Member> memberMap = memberPairs.stream()
			.collect(Collectors.toMap(MemberEmailPair::email, MemberEmailPair::member));

		// 멤버 추가 로직
		List<MemberResult> results = new ArrayList<>();
		int successCount = 0;

		for (MemberAdditionItem item : members) {
			Member member = memberMap.get(item.email());
			if (member == null) {
				results.add(new MemberResult(item.email(), item.role(), "FAILED", "해당 회원이 존재하지 않습니다"));
				continue;
			}

			try {
				course.addMember(member.getId(), item.role());
				results.add(new MemberResult(item.email(), item.role(), "SUCCESS", "과정 멤버 추가 성공"));
				successCount++;
			} catch (Exception e) {
				results.add(new MemberResult(item.email(), item.role(), "FAILED", e.getMessage()));
			}
		}

		commandRepository.save(course);

		return new CourseMemberAddition.Response(members.size(), successCount, members.size() - successCount, results);

	}

	@Override
	public void removeMemberFromCourse(Long courseId, Long memberId) {
		Course course = queryRepository.findManagedCourseById(courseId, userContext.getCurrentMemberId())
			.orElseThrow(() -> new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));
		course.removeMember(memberId);
		commandRepository.save(course);
	}
}
