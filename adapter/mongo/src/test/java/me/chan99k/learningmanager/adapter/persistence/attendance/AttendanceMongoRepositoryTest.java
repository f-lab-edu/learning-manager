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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import me.chan99k.learningmanager.adapter.persistence.attendance.config.TestMongoConfig;
import me.chan99k.learningmanager.adapter.persistence.attendance.documents.AttendanceDocument;
import me.chan99k.learningmanager.attendance.Attendance;
import me.chan99k.learningmanager.attendance.AttendanceStatus;

@DataMongoTest
@Testcontainers
@Import(TestMongoConfig.class)
@DisplayName("AttendanceMongoRepository 테스트")
class AttendanceMongoRepositoryTest {

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
	private MongoTemplate mongoTemplate;

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@BeforeEach
	void setUp() {
		attendanceMongoRepository.deleteAll();

		// 테스트를 위해 인덱스를 강제로 생성
		ensureUniqueIndex();
	}

	private void ensureUniqueIndex() {
		try {
			// AttendanceDocument 클래스를 기반으로 컬렉션명 확인
			mongoTemplate.indexOps(AttendanceDocument.class)
				.ensureIndex(new org.springframework.data.mongodb.core.index.CompoundIndexDefinition(
					new org.bson.Document()
						.append("sessionId", 1)
						.append("memberId", 1)
				).unique().named("session_member_unique_idx"));
		} catch (Exception e) {
			// 인덱스가 이미 존재할 수 있음
		}
	}

	@Test
	@DisplayName("출석 기록 저장 및 조회 - 성공")
	void saveAndFindAttendance_Success() {
		AttendanceDocument attendance = createTestAttendanceDocument(MEMBER_ID_1, SESSION_ID_1,
			AttendanceStatus.PRESENT);

		AttendanceDocument saved = attendanceMongoRepository.save(attendance);

		assertThat(saved.get_id()).isNotNull();
		assertThat(saved.getMemberId()).isEqualTo(MEMBER_ID_1);
		assertThat(saved.getSessionId()).isEqualTo(SESSION_ID_1);
		assertThat(saved.getFinalStatus()).isEqualTo(AttendanceStatus.PRESENT);
	}

	@Test
	@DisplayName("세션 ID와 멤버 ID로 출석 기록 조회 - 성공")
	void findBySessionIdAndMemberId_Success() {
		AttendanceDocument attendance = createTestAttendanceDocument(MEMBER_ID_1, SESSION_ID_1,
			AttendanceStatus.PRESENT);
		attendanceMongoRepository.save(attendance);

		Optional<AttendanceDocument> result = attendanceMongoRepository
			.findBySessionIdAndMemberId(SESSION_ID_1, MEMBER_ID_1);

		assertThat(result).isPresent();
		AttendanceDocument found = result.get();
		assertThat(found.getSessionId()).isEqualTo(SESSION_ID_1);
		assertThat(found.getMemberId()).isEqualTo(MEMBER_ID_1);
		assertThat(found.getFinalStatus()).isEqualTo(AttendanceStatus.PRESENT);
	}

	@Test
	@DisplayName("존재하지 않는 기록 조회 시 빈 결과")
	void findBySessionIdAndMemberId_NotFound() {
		Optional<AttendanceDocument> result = attendanceMongoRepository.findBySessionIdAndMemberId(999L, 999L);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("멤버 ID로 모든 출석 기록 조회 - 성공")
	void findByMemberId_Success() {
		AttendanceDocument attendance1 = createTestAttendanceDocument(MEMBER_ID_1, SESSION_ID_1,
			AttendanceStatus.PRESENT);
		AttendanceDocument attendance2 = createTestAttendanceDocument(MEMBER_ID_1, SESSION_ID_2,
			AttendanceStatus.ABSENT);
		AttendanceDocument attendance3 = createTestAttendanceDocument(MEMBER_ID_2, SESSION_ID_3,
			AttendanceStatus.PRESENT);

		attendanceMongoRepository.saveAll(List.of(attendance1, attendance2, attendance3));

		List<AttendanceDocument> result = attendanceMongoRepository.findByMemberId(MEMBER_ID_1);

		assertThat(result).hasSize(2);
		assertThat(result)
			.extracting(AttendanceDocument::getSessionId)
			.containsExactlyInAnyOrder(SESSION_ID_1, SESSION_ID_2);
		assertThat(result)
			.extracting(AttendanceDocument::getMemberId)
			.allMatch(memberId -> memberId.equals(MEMBER_ID_1));
	}

	@Test
	@DisplayName("멤버 ID와 세션 ID 목록으로 출석 기록 조회 - 성공")
	void findByMemberIdAndSessionIdIn_Success() {
		AttendanceDocument attendance1 = createTestAttendanceDocument(MEMBER_ID_1, SESSION_ID_1,
			AttendanceStatus.PRESENT);
		AttendanceDocument attendance2 = createTestAttendanceDocument(MEMBER_ID_1, SESSION_ID_2,
			AttendanceStatus.ABSENT);
		AttendanceDocument attendance3 = createTestAttendanceDocument(MEMBER_ID_1, SESSION_ID_3, AttendanceStatus.LATE);

		attendanceMongoRepository.saveAll(List.of(attendance1, attendance2, attendance3));

		List<Long> sessionIds = List.of(SESSION_ID_1, SESSION_ID_2);
		List<AttendanceDocument> result = attendanceMongoRepository.findByMemberIdAndSessionIdIn(MEMBER_ID_1,
			sessionIds);

		assertThat(result).hasSize(2);
		assertThat(result)
			.extracting(AttendanceDocument::getSessionId)
			.containsExactlyInAnyOrder(SESSION_ID_1, SESSION_ID_2);
		assertThat(result)
			.extracting(AttendanceDocument::getFinalStatus)
			.containsExactlyInAnyOrder(AttendanceStatus.PRESENT, AttendanceStatus.ABSENT);
	}

	@Test
	@DisplayName("빈 세션 ID 목록으로 조회 시 빈 결과")
	void findByMemberIdAndSessionIdIn_EmptySessionIds() {
		AttendanceDocument attendance = createTestAttendanceDocument(MEMBER_ID_1, SESSION_ID_1,
			AttendanceStatus.PRESENT);
		attendanceMongoRepository.save(attendance);

		List<AttendanceDocument> result = attendanceMongoRepository.findByMemberIdAndSessionIdIn(MEMBER_ID_1,
			List.of());

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("프로젝션 쿼리로 필요한 필드만 조회 - 성공")
	void findProjectionByMemberIdAndSessionIds_Success() {
		AttendanceDocument attendance1 = createTestAttendanceDocument(MEMBER_ID_1, SESSION_ID_1,
			AttendanceStatus.PRESENT);
		AttendanceDocument attendance2 = createTestAttendanceDocument(MEMBER_ID_1, SESSION_ID_2,
			AttendanceStatus.ABSENT);

		attendanceMongoRepository.saveAll(List.of(attendance1, attendance2));

		List<Long> sessionIds = List.of(SESSION_ID_1, SESSION_ID_2);
		List<AttendanceDocument> result = attendanceMongoRepository.findProjectionByMemberIdAndSessionIds(MEMBER_ID_1,
			sessionIds);

		assertThat(result).hasSize(2);

		// 프로젝션된 필드 확인
		for (AttendanceDocument doc : result) {
			assertThat(doc.get_id()).isNotNull();
			assertThat(doc.getSessionId()).isNotNull();
			assertThat(doc.getMemberId()).isNotNull();
			assertThat(doc.getFinalStatus()).isNotNull();
		}
	}

	@Test
	@DisplayName("복합 유니크 인덱스 - 동일한 세션과 멤버 조합 중복 저장 방지")
	void uniqueConstraint_SessionIdAndMemberId_DuplicateThrowsException() {
		AttendanceDocument attendance1 = createTestAttendanceDocument(MEMBER_ID_1, SESSION_ID_1,
			AttendanceStatus.PRESENT);
		AttendanceDocument attendance2 = createTestAttendanceDocument(MEMBER_ID_1, SESSION_ID_1,
			AttendanceStatus.ABSENT);

		attendanceMongoRepository.save(attendance1);

		assertThatThrownBy(() -> attendanceMongoRepository.save(attendance2))
			.isInstanceOf(org.springframework.dao.DuplicateKeyException.class);
	}

	@Test
	@DisplayName("다른 멤버의 동일 세션 출석 기록은 저장 가능")
	void uniqueConstraint_DifferentMembers_SameSession_AllowsSave() {
		AttendanceDocument attendance1 = createTestAttendanceDocument(MEMBER_ID_1, SESSION_ID_1,
			AttendanceStatus.PRESENT);
		AttendanceDocument attendance2 = createTestAttendanceDocument(MEMBER_ID_2, SESSION_ID_1,
			AttendanceStatus.ABSENT);

		assertThatCode(() -> {
			attendanceMongoRepository.save(attendance1);
			attendanceMongoRepository.save(attendance2);
		}).doesNotThrowAnyException();

		// 둘 다 저장되었는지 확인
		assertThat(attendanceMongoRepository.findBySessionIdAndMemberId(SESSION_ID_1, MEMBER_ID_1)).isPresent();
		assertThat(attendanceMongoRepository.findBySessionIdAndMemberId(SESSION_ID_1, MEMBER_ID_2)).isPresent();
	}

	@Test
	@DisplayName("출석 상태별 조회 성능 테스트")
	void performanceTest_FindByStatus() {
		List<AttendanceDocument> documents = List.of(
			createTestAttendanceDocument(MEMBER_ID_1, 1L, AttendanceStatus.PRESENT),
			createTestAttendanceDocument(MEMBER_ID_1, 2L, AttendanceStatus.ABSENT),
			createTestAttendanceDocument(MEMBER_ID_1, 3L, AttendanceStatus.PRESENT),
			createTestAttendanceDocument(MEMBER_ID_2, 4L, AttendanceStatus.LATE),
			createTestAttendanceDocument(MEMBER_ID_2, 5L, AttendanceStatus.LEFT_EARLY)
		);

		attendanceMongoRepository.saveAll(documents);

		long startTime = System.currentTimeMillis();
		List<AttendanceDocument> result = attendanceMongoRepository.findByMemberId(MEMBER_ID_1);
		long endTime = System.currentTimeMillis();

		assertThat(result).hasSize(3);
		// CI 환경에서 Testcontainers 오버헤드를 고려하여 임계값을 500ms로 설정
		assertThat(endTime - startTime).isLessThan(500);
	}

	private AttendanceDocument createTestAttendanceDocument(Long memberId, Long sessionId, AttendanceStatus status) {
		Attendance attendance = Attendance.create(sessionId, memberId);

		if (status == AttendanceStatus.PRESENT) {
			attendance.checkIn(java.time.Clock.systemUTC());
		}

		return AttendanceDocument.from(attendance);
	}
}