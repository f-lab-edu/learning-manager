package me.chan99k.learningmanager.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.auth.PasswordEncoder;
import me.chan99k.learningmanager.auth.SignUpConfirmTokenProvider;
import me.chan99k.learningmanager.exception.DomainException;

@Service
@Transactional
public class MemberRegisterService implements MemberRegistration, AccountAddition, SignUpConfirmation {

	private static final Logger log = LoggerFactory.getLogger(MemberRegisterService.class);

	private final MemberCommandRepository memberCommandRepository;
	private final MemberQueryRepository memberQueryRepository;
	private final NicknameGenerator nicknameGenerator;
	private final PasswordEncoder passwordEncoder;
	private final SignUpConfirmTokenProvider signUpConfirmTokenProvider;
	private final EmailSender emailSender;

	public MemberRegisterService(
		MemberCommandRepository memberCommandRepository,
		MemberQueryRepository memberQueryRepository,
		NicknameGenerator nicknameGenerator, PasswordEncoder passwordEncoder,
		SignUpConfirmTokenProvider signUpConfirmTokenProvider, EmailSender emailSender
	) {
		this.memberCommandRepository = memberCommandRepository;
		this.memberQueryRepository = memberQueryRepository;
		this.nicknameGenerator = nicknameGenerator;
		this.passwordEncoder = passwordEncoder;
		this.signUpConfirmTokenProvider = signUpConfirmTokenProvider;
		this.emailSender = emailSender;
	}

	@Override
	public MemberRegistration.Response register(MemberRegistration.Request request) {
		Member newMember = Member.registerDefault(nicknameGenerator);
		newMember.addAccount(request.email());

		// 해싱 후 계정에 Credential로 추가
		String hashedPassword = passwordEncoder.encode(request.rawPassword());
		Account account = newMember.findAccountByEmail(Email.of(request.email()));
		account.addCredential(Credential.ofPassword(hashedPassword));

		Member saved = memberCommandRepository.save(newMember);

		// 이메일 인증 토큰 생성 및 발송
		log.info("EmailSender 클래스: {}", emailSender.getClass().getName());
		String confirmToken = signUpConfirmTokenProvider.createAndStoreToken(request.email());
		emailSender.sendSignUpConfirmEmail(request.email(), confirmToken);

		return new MemberRegistration.Response(saved.getId());
	}

	@Override
	public void activateSignUpMember(SignUpConfirmation.Request request) {
		Email email = Email.of(signUpConfirmTokenProvider.validateAndGetEmail(request.token()));
		Member member = memberQueryRepository.findByEmail(email)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		member.activate();

		Account account = member.findAccountByEmail(email);
		account.activate();

		memberCommandRepository.save(member);

		signUpConfirmTokenProvider.removeToken(request.token());
	}
}
