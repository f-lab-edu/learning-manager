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
@Import({AttendanceQueryAdapter.class, TestMongoConfig.class})
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
	@DisplayName("특정 멤버의 모든 출석 기록 조회 - 성공")
	void findByMemberId_Success() {
		List<Attendance> result = attendanceQueryAdapter.findByMemberId(MEMBER_ID_1);

		assertThat(result).hasSize(2);
		assertThat(result)
			.extracting(Attendance::getSessionId)
			.containsExactlyInAnyOrder(SESSION_ID_1, SESSION_ID_2);
		assertThat(result)
			.extracting(Attendance::getFinalStatus)
			.containsExactlyInAnyOrder(AttendanceStatus.PRESENT, AttendanceStatus.ABSENT);
	}

	@Test
	@DisplayName("존재하지 않는 멤버 조회 시 빈 목록")
	void findByMemberId_NotFound() {
		List<Attendance> result = attendanceQueryAdapter.findByMemberId(999L);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("특정 멤버와 세션 ID 목록으로 출석 기록 조회 - 성공")
	void findByMemberIdAndSessionIds_Success() {
		List<Long> sessionIds = List.of(SESSION_ID_1, SESSION_ID_2);

		List<Attendance> result = attendanceQueryAdapter.findByMemberIdAndSessionIds(MEMBER_ID_1, sessionIds);

		assertThat(result).hasSize(2);
		assertThat(result)
			.extracting(Attendance::getSessionId)
			.containsExactlyInAnyOrder(SESSION_ID_1, SESSION_ID_2);
	}

	@Test
	@DisplayName("빈 세션 ID 목록으로 조회 시 빈 결과")
	void findByMemberIdAndSessionIds_EmptySessionIds() {
		List<Attendance> result = attendanceQueryAdapter.findByMemberIdAndSessionIds(MEMBER_ID_1, List.of());

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("일부만 매칭되는 세션 ID 목록으로 조회")
	void findByMemberIdAndSessionIds_PartialMatch() {
		List<Long> sessionIds = List.of(SESSION_ID_1, 999L); // SESSION_ID_1만 존재

		List<Attendance> result = attendanceQueryAdapter.findByMemberIdAndSessionIds(MEMBER_ID_1, sessionIds);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getSessionId()).isEqualTo(SESSION_ID_1);
	}

	@Test
	@DisplayName("프로젝션으로 출석 기록 조회 - 성공")
	void findAttendanceProjectionByMemberIdAndSessionIds_Success() {
		List<Long> sessionIds = List.of(SESSION_ID_1, SESSION_ID_2);

		List<AttendanceQueryRepository.AttendanceProjection> result =
			attendanceQueryAdapter.findAttendanceProjectionByMemberIdAndSessionIds(MEMBER_ID_1, sessionIds);

		assertThat(result).hasSize(2);

		AttendanceQueryRepository.AttendanceProjection first = result.get(0);
		assertThat(first.attendanceId()).isNotNull();
		assertThat(first.sessionId()).isIn(SESSION_ID_1, SESSION_ID_2);
		assertThat(first.memberId()).isEqualTo(MEMBER_ID_1);
		assertThat(first.finalStatus()).isIn(AttendanceStatus.PRESENT, AttendanceStatus.ABSENT);
	}

	@Test
	@DisplayName("프로젝션 조회 - 빈 세션 ID 목록")
	void findAttendanceProjectionByMemberIdAndSessionIds_EmptySessionIds() {
		List<AttendanceQueryRepository.AttendanceProjection> result =
			attendanceQueryAdapter.findAttendanceProjectionByMemberIdAndSessionIds(MEMBER_ID_1, List.of());

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("여러 멤버의 출석 기록이 분리되어 조회됨")
	void findByMemberId_MultipleMembers_Separated() {
		List<Attendance> member1Result = attendanceQueryAdapter.findByMemberId(MEMBER_ID_1);
		List<Attendance> member2Result = attendanceQueryAdapter.findByMemberId(MEMBER_ID_2);

		assertThat(member1Result).hasSize(2);
		assertThat(member2Result).hasSize(1);

		assertThat(member1Result)
			.extracting(Attendance::getMemberId)
			.allMatch(memberId -> memberId.equals(MEMBER_ID_1));

		assertThat(member2Result)
			.extracting(Attendance::getMemberId)
			.allMatch(memberId -> memberId.equals(MEMBER_ID_2));
	}

	private AttendanceDocument createAttendanceDocument(Long memberId, Long sessionId, AttendanceStatus status) {
		Attendance attendance = Attendance.create(sessionId, memberId);

		if (status == AttendanceStatus.PRESENT) {
			attendance.checkIn(java.time.Clock.systemUTC());
		}

		return AttendanceDocument.from(attendance);
	}
}


