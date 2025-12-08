package me.chan99k.learningmanager.attendance;

import java.time.Instant;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.session.Session;
import me.chan99k.learningmanager.session.SessionProblemCode;
import me.chan99k.learningmanager.session.SessionQueryRepository;

/**
 * 추후 admin 앱으로 분리해야 할 가능성 있음
 */
@Service
@Transactional
public class AttendanceTokenService implements GenerateAttendanceToken {
	private final SessionQueryRepository sessionQueryRepository;
	private final QRCodeGenerator qrCodeGenerator;

	public AttendanceTokenService(SessionQueryRepository sessionQueryRepository, QRCodeGenerator qrCodeGenerator) {
		this.sessionQueryRepository = sessionQueryRepository;
		this.qrCodeGenerator = qrCodeGenerator;
	}

	@Override
	@Transactional(readOnly = true)
	public Response generate(Long requestedBy, Request request) {
		Session session = sessionQueryRepository.findById(request.sessionId())
			.orElseThrow(() -> new DomainException(SessionProblemCode.SESSION_NOT_FOUND));

		if (!session.isRootSession()) {
			throw new DomainException(AttendanceProblemCode.ONLY_ROOT_SESSION_ALLOWED);
		}

		Instant checkOutExpiresAt = session.getScheduledEndAt()
			.atZone(ZoneId.of("Asia/Seoul"))
			.toLocalDate()
			.plusDays(1)
			.atStartOfDay(ZoneId.of("Asia/Seoul"))
			.toInstant();

		String token = qrCodeGenerator.generateQrCode(
			session.getId(),
			session.getScheduledEndAt()
		);

		String checkInUrl = "/api/v1/attendance/check-in/" + token;
		String checkOutUrl = "/api/v1/attendance/check-out/" + token;

		return new Response(
			token,
			checkInUrl,
			checkOutUrl,
			session.getScheduledEndAt(),
			checkOutExpiresAt,
			session.getCourseId(),
			session.getCurriculumId(),
			session.getId(),
			session.getTitle()
		);
	}
}
