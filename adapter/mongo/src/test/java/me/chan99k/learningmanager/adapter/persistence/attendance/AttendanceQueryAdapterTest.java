package me.chan99k.learningmanager.adapter.persistence.attendance;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import me.chan99k.learningmanager.adapter.persistence.attendance.config.TestMongoConfig;
import me.chan99k.learningmanager.adapter.persistence.attendance.documents.AttendanceDocument;
import me.chan99k.learningmanager.attendance.Attendance;
import me.chan99k.learningmanager.attendance.AttendanceQueryRepository;
import me.chan99k.learningmanager.attendance.AttendanceStatus;

@DataMongoTest
@Testcontainers
@Import({AttendanceQueryAdapter.class, CustomAttendanceMongoRepositoryImpl.class, TestMongoConfig.class})
@DisplayName("AttendanceQueryAdapter 통합 테스트")
class AttendanceQueryAdapterTest {

	private static final Long MEMBER_ID_1 = 123L;
	private static final Long MEMBER_ID_2 = 124L;
	private static final Long SESSION_ID_1 = 101L;
	private static final Long SESSION_ID_2 = 102L;
	private static final Long SESSION_ID_3 = 103L;

	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

	@Autowired
	private AttendanceMongoRepository attendanceMongoRepository;

	@Autowired
	private AttendanceQueryAdapter attendanceQueryAdapter;

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@BeforeEach
	void setUp() {
		attendanceMongoRepository.deleteAll();

		AttendanceDocument attendance1 = createAttendanceDocument(MEMBER_ID_1, SESSION_ID_1, AttendanceStatus.PRESENT);
		AttendanceDocument attendance2 = createAttendanceDocument(MEMBER_ID_1, SESSION_ID_2, AttendanceStatus.ABSENT);
		AttendanceDocument attendance3 = createAttendanceDocument(MEMBER_ID_2, SESSION_ID_3, AttendanceStatus.PRESENT);

		attendanceMongoRepository.saveAll(List.of(attendance1, attendance2, attendance3));
	}

	@Test
	@DisplayName("특정 세션과 멤버로 출석 기록 조회 - 성공")
	void findBySessionIdAndMemberId_Success() {
		Optional<Attendance> result = attendanceQueryAdapter.findBySessionIdAndMemberId(SESSION_ID_1, MEMBER_ID_1);

		assertThat(result).isPresent();
		Attendance attendance = result.get();
		assertThat(attendance.getSessionId()).isEqualTo(SESSION_ID_1);
		assertThat(attendance.getMemberId()).isEqualTo(MEMBER_ID_1);
		assertThat(attendance.getFinalStatus()).isEqualTo(AttendanceStatus.PRESENT);
	}

	@Test
	@DisplayName("존재하지 않는 세션과 멤버로 조회 시 빈 결과")
	void findBySessionIdAndMemberId_NotFound() {
		Optional<Attendance> result = attendanceQueryAdapter.findBySessionIdAndMemberId(999L, 999L);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("단일 멤버 출석 통계 조회 - 성공")
	void findMemberAttendanceWithStats_Success() {
		List<Long> sessionIds = List.of(SESSION_ID_1, SESSION_ID_2);

		AttendanceQueryRepository.MemberAttendanceResult result =
			attendanceQueryAdapter.findMemberAttendanceWithStats(MEMBER_ID_1, sessionIds);

		assertThat(result.memberId()).isEqualTo(MEMBER_ID_1);
		assertThat(result.attendances()).hasSize(2);
		assertThat(result.stats().total()).isEqualTo(2);
		assertThat(result.stats().present()).isEqualTo(1);
		assertThat(result.stats().absent()).isEqualTo(1);
		assertThat(result.stats().rate()).isEqualTo(50.0);
	}

	@Test
	@DisplayName("단일 멤버 출석 통계 조회 - 빈 세션 목록")
	void findMemberAttendanceWithStats_EmptySessionIds() {
		AttendanceQueryRepository.MemberAttendanceResult result =
			attendanceQueryAdapter.findMemberAttendanceWithStats(MEMBER_ID_1, List.of());

		assertThat(result.memberId()).isEqualTo(MEMBER_ID_1);
		assertThat(result.attendances()).isEmpty();
		assertThat(result.stats().total()).isZero();
	}

	@Test
	@DisplayName("여러 멤버 출석 통계 조회 - 성공")
	void findAllMembersAttendanceWithStats_Success() {
		List<Long> sessionIds = List.of(SESSION_ID_1, SESSION_ID_2, SESSION_ID_3);
		List<Long> memberIds = List.of(MEMBER_ID_1, MEMBER_ID_2);

		List<AttendanceQueryRepository.MemberAttendanceResult> results =
			attendanceQueryAdapter.findAllMembersAttendanceWithStats(sessionIds, memberIds);

		assertThat(results).hasSize(2);

		var member1Result = results.stream()
			.filter(r -> r.memberId().equals(MEMBER_ID_1))
			.findFirst()
			.orElseThrow();
		assertThat(member1Result.attendances()).hasSize(2);
		assertThat(member1Result.stats().rate()).isEqualTo(50.0);

		var member2Result = results.stream()
			.filter(r -> r.memberId().equals(MEMBER_ID_2))
			.findFirst()
			.orElseThrow();
		assertThat(member2Result.attendances()).hasSize(1);
		assertThat(member2Result.stats().rate()).isEqualTo(100.0);
	}

	@Test
	@DisplayName("여러 멤버 출석 통계 조회 - 빈 목록")
	void findAllMembersAttendanceWithStats_EmptyLists() {
		List<AttendanceQueryRepository.MemberAttendanceResult> results =
			attendanceQueryAdapter.findAllMembersAttendanceWithStats(List.of(), List.of());

		assertThat(results).isEmpty();
	}

	private AttendanceDocument createAttendanceDocument(Long memberId, Long sessionId, AttendanceStatus status) {
		Attendance attendance = Attendance.create(sessionId, memberId);

		if (status == AttendanceStatus.PRESENT) {
			attendance.checkIn(java.time.Clock.systemUTC());
		}

		return AttendanceDocument.from(attendance);
	}
}
