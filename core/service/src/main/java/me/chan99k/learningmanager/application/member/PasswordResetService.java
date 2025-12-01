package me.chan99k.learningmanager.application.member;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.application.member.provides.AccountPasswordReset;
import me.chan99k.learningmanager.domain.exception.DomainException;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.EmailSender;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;
import me.chan99k.learningmanager.domain.member.PasswordEncoder;

@Service
@Transactional
public class PasswordResetService implements AccountPasswordReset {
	private final MemberQueryRepository memberQueryRepository;
	private final MemberCommandRepository memberCommandRepository;
	private final PasswordResetTokenProvider passwordResetTokenProvider;
	private final EmailSender emailSender;
	private final PasswordEncoder passwordEncoder;
	private final Executor emailTaskExecutor;

	public PasswordResetService(MemberQueryRepository memberQueryRepository,
		MemberCommandRepository memberCommandRepository, PasswordResetTokenProvider passwordResetTokenProvider,
		EmailSender emailSender, PasswordEncoder passwordEncoder,
		@Qualifier("emailTaskExecutor") Executor emailTaskExecutor) {
		this.memberQueryRepository = memberQueryRepository;
		this.memberCommandRepository = memberCommandRepository;
		this.passwordResetTokenProvider = passwordResetTokenProvider;
		this.emailSender = emailSender;
		this.passwordEncoder = passwordEncoder;
		this.emailTaskExecutor = emailTaskExecutor;
	}

	@Override
	public RequestResetResponse requestReset(RequestResetRequest request) {
		var email = Email.of(request.email());
		boolean isEmailExists = memberQueryRepository.findByEmail(email).isPresent();

		if (isEmailExists) {
			String token = passwordResetTokenProvider.createResetToken(request.email());

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
	public ConfirmResetResponse confirmReset(ConfirmResetRequest request) {
		String token = request.token();
		String newPassword = request.newPassword();

		validatePasswordResetToken(token);

		Email email = passwordResetTokenProvider.getEmailFromResetToken(token);

		Member member = memberQueryRepository.findByEmail(email)
			.orElseThrow(() -> new DomainException(MemberProblemCode.PASSWORD_RESET_EMAIL_NOT_FOUND));

		if (member.getAccounts().isEmpty()) {
			throw new DomainException(MemberProblemCode.ACCOUNT_NOT_FOUND);
		}

		Long accountId = member.findAccountByEmail(email).getId();

		member.changeAccountPassword(accountId, newPassword, passwordEncoder);

		memberCommandRepository.save(member);

		// 토큰 사용 후 즉시 삭제 (일회용)
		passwordResetTokenProvider.invalidateAfterUse(token);

		return new ConfirmResetResponse();
	}

	public boolean validatePasswordResetToken(String token) {
		// 토큰 유효성 검증
		if (!passwordResetTokenProvider.validateResetToken(token)) {
			throw new DomainException(MemberProblemCode.INVALID_PASSWORD_RESET_TOKEN);
		}
		return true;
	}

	@Override
	public TokenVerificationResponse verifyResetToken(String token) {
		// 토큰 유효성 검증
		validatePasswordResetToken(token);

		// 토큰에서 이메일 추출
		Email email = passwordResetTokenProvider.getEmailFromResetToken(token);

		// 해당 이메일의 회원이 존재하는지 확인
		Member member = memberQueryRepository.findByEmail(email)
			.orElseThrow(() -> new DomainException(MemberProblemCode.PASSWORD_RESET_EMAIL_NOT_FOUND));

		return new TokenVerificationResponse(
			true,
			email.address(),
			token,
			"토큰이 유효합니다. 새 비밀번호를 설정하세요."
		);
	}
}
