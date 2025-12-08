package me.chan99k.learningmanager.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.authentication.PasswordEncoder;
import me.chan99k.learningmanager.authentication.PasswordResetTokenProvider;
import me.chan99k.learningmanager.exception.DomainException;

@Service
@Transactional
public class PasswordResetService
	implements PasswordResetRequest, PasswordResetVerification, PasswordResetConfirmation {
	private final MemberQueryRepository memberQueryRepository;
	private final MemberCommandRepository memberCommandRepository;
	private final PasswordResetTokenProvider passwordResetTokenProvider;
	private final PasswordEncoder passwordEncoder;
	private final EmailSender emailSender;

	public PasswordResetService(
		MemberQueryRepository memberQueryRepository,
		MemberCommandRepository memberCommandRepository,
		PasswordResetTokenProvider passwordResetTokenProvider,
		PasswordEncoder passwordEncoder,
		EmailSender emailSender
	) {
		this.memberQueryRepository = memberQueryRepository;
		this.memberCommandRepository = memberCommandRepository;
		this.passwordResetTokenProvider = passwordResetTokenProvider;
		this.passwordEncoder = passwordEncoder;
		this.emailSender = emailSender;
	}

	@Override
	public void requestReset(PasswordResetRequest.Request request) {
		Email email = Email.of(request.email());
		memberQueryRepository.findByEmail(email)
			.orElseThrow(() -> new DomainException(MemberProblemCode.PASSWORD_RESET_EMAIL_NOT_FOUND));

		String token = passwordResetTokenProvider.createAndStoreToken(email);

		emailSender.sendPasswordResetEmail(email.address(), token);
	}

	@Override
	public Response verifyResetToken(PasswordResetVerification.Request request) {
		boolean isValid = passwordResetTokenProvider.validateResetToken(request.token());

		if (!isValid) {
			return new PasswordResetVerification.Response(false, null);
		}

		String email = passwordResetTokenProvider.getEmailFromResetToken(request.token());

		return new PasswordResetVerification.Response(true, email);
	}

	@Override
	public void confirmReset(PasswordResetConfirmation.Request request) {
		// 1. 토큰 검증
		if (!passwordResetTokenProvider.validateResetToken(request.token())) {
			throw new DomainException(MemberProblemCode.INVALID_PASSWORD_RESET_TOKEN);
		}

		// 2. 이메일로 회원 조회
		String emailStr = passwordResetTokenProvider.getEmailFromResetToken(request.token());
		Email email = Email.of(emailStr);
		Member member = memberQueryRepository.findByEmail(email)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		// 3. 비밀번호 변경
		String hashedPassword = passwordEncoder.encode(request.newPassword());
		Account account = member.findAccountByEmail(email);
		account.changePasswordCredential(hashedPassword);

		memberCommandRepository.save(member);

		passwordResetTokenProvider.invalidateAfterUse(request.token());
	}

}
