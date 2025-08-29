package me.chan99k.learningmanager.application.member;

import java.time.Duration;
import java.util.concurrent.Executor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.adapter.auth.PasswordResetTokenManager;
import me.chan99k.learningmanager.application.member.provides.AccountPasswordReset;
import me.chan99k.learningmanager.application.member.requires.MemberCommandRepository;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.EmailSender;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;
import me.chan99k.learningmanager.domain.member.PasswordEncoder;

@Service
@Transactional
public class PasswordResetService implements AccountPasswordReset {
	private static final Duration TOKEN_EXPIRATION = Duration.ofHours(1); // 1시간 만료 TODO :: .yml 로 토큰 만료 시간 관리

	private final MemberQueryRepository memberQueryRepository;
	private final MemberCommandRepository memberCommandRepository;
	private final PasswordResetTokenManager passwordResetTokenManager;
	private final EmailSender emailSender;
	private final PasswordEncoder passwordEncoder;
	private final Executor emailTaskExecutor;

	public PasswordResetService(MemberQueryRepository memberQueryRepository,
		MemberCommandRepository memberCommandRepository, PasswordResetTokenManager passwordResetTokenManager,
		EmailSender emailSender, PasswordEncoder passwordEncoder, Executor emailTaskExecutor) {
		this.memberQueryRepository = memberQueryRepository;
		this.memberCommandRepository = memberCommandRepository;
		this.passwordResetTokenManager = passwordResetTokenManager;
		this.emailSender = emailSender;
		this.passwordEncoder = passwordEncoder;
		this.emailTaskExecutor = emailTaskExecutor;
	}

	@Override
	public RequestResetResponse requestReset(RequestResetRequest request) {
		var email = Email.of(request.email());
		boolean isEmailExists = memberQueryRepository.findByEmail(email).isPresent();

		if (isEmailExists) {
			String token = passwordResetTokenManager.generateAndStoreToken(request.email(), TOKEN_EXPIRATION);

			// 이메일 발송은 비동기로 처리
			emailTaskExecutor.execute(() -> {
				emailSender.sendPasswordResetEmail(request.email(), token);
			});

			// 즉시 응답 반환
			String message = String.format("가입시 사용한 이메일: %s 로 비밀번호 재설정 메일을 발송했습니다.", request.email());
			return new RequestResetResponse(message);
		} else {
			throw new DomainException(MemberProblemCode.PASSWORD_RESET_EMAIL_NOT_FOUND);
		}
	}

	@Override
	public ConfirmResetResponse confirmReset(String token, String newPassword) {
		// 토큰으로부터 이메일 추출
		String emailString = passwordResetTokenManager.getEmailByToken(token);
		if (emailString == null) {
			throw new DomainException(MemberProblemCode.INVALID_PASSWORD_RESET_TOKEN);
		}

		Email email = Email.of(emailString);

		Member member = memberQueryRepository.findByEmail(email)
			.orElseThrow(() -> new DomainException(MemberProblemCode.PASSWORD_RESET_EMAIL_NOT_FOUND));

		if (member.getAccounts().isEmpty()) {
			throw new DomainException(MemberProblemCode.ACCOUNT_NOT_FOUND);
		}

		Long accountId = member.getAccounts().get(0).getId();

		member.changeAccountPassword(accountId, newPassword, passwordEncoder);

		// 토큰 사용 후 즉시 삭제 (일회용)
		passwordResetTokenManager.removeToken(token);

		memberCommandRepository.save(member);

		return new ConfirmResetResponse();
	}

	public boolean validatePasswordResetToken(String token) {
		// 토큰 유효성 검증
		if (!passwordResetTokenManager.validateToken(token)) {
			throw new DomainException(MemberProblemCode.INVALID_PASSWORD_RESET_TOKEN);
		}
		return true;
	}
}
