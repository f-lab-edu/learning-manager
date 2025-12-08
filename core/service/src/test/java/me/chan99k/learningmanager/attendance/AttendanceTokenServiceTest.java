package me.chan99k.learningmanager.attendance;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.session.Session;
import me.chan99k.learningmanager.session.SessionQueryRepository;

@ExtendWith(MockitoExtension.class)
class AttendanceTokenServiceTest {
	private static final Long SESSION_ID = 1L;
	private static final Long COURSE_ID = 10L;
	private static final Long MEMBER_ID = 100L;
	private static final Instant SESSION_END = Instant.parse("2025-12-08T18:00:00Z");

	@Mock
	private SessionQueryRepository sessionQueryRepository;

	@Mock
	private QRCodeGenerator qrCodeGenerator;

	@InjectMocks
	private AttendanceTokenService attendanceTokenService;

	@Test
	@DisplayName("[Success] 루트 세션에 대해 토큰 생성 성공")
	void generateToken_success() {
		Session session = createRootSession();
		when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
		when(qrCodeGenerator.generateQrCode(eq(SESSION_ID), any(Instant.class)))
			.thenReturn("SESSION_1_1733698800000");

		var request = new GenerateAttendanceToken.Request(SESSION_ID);

		var response = attendanceTokenService.generate(MEMBER_ID, request);

		assertThat(response.token()).isEqualTo("SESSION_1_1733698800000");
		assertThat(response.sessionId()).isEqualTo(SESSION_ID);
		assertThat(response.checkInUrl()).contains("/check-in/");
		assertThat(response.checkOutUrl()).contains("/check-out/");
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 세션")
	void generateToken_sessionNotFound() {
		when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.empty());
		var request = new GenerateAttendanceToken.Request(SESSION_ID);

		assertThatThrownBy(() -> attendanceTokenService.generate(MEMBER_ID, request))
			.isInstanceOf(DomainException.class);
	}

	@Test
	@DisplayName("[Failure] 하위 세션에 대해 토큰 생성 시도")
	void generateToken_childSessionNotAllowed() {
		Session childSession = createChildSession();
		when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.of(childSession));
		var request = new GenerateAttendanceToken.Request(SESSION_ID);

		assertThatThrownBy(() -> attendanceTokenService.generate(MEMBER_ID, request))
			.isInstanceOf(DomainException.class);
	}

	private Session createRootSession() {
		Session session = mock(Session.class);
		when(session.getId()).thenReturn(SESSION_ID);
		when(session.isRootSession()).thenReturn(true);
		when(session.getScheduledEndAt()).thenReturn(SESSION_END);
		when(session.getTitle()).thenReturn("Spring Boot 스터디 1회차");
		when(session.getCourseId()).thenReturn(COURSE_ID);
		when(session.getCurriculumId()).thenReturn(null);
		return session;
	}

	private Session createChildSession() {
		Session session = mock(Session.class);
		when(session.isRootSession()).thenReturn(false);  // 하위 세션임
		return session;
	}

}