package me.chan99k.learningmanager.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.auth.PasswordEncoder;

@Service
@Transactional
public class MemberRegisterService implements MemberRegistration, AccountAddition, SignUpConfirmation {

	private final MemberCommandRepository memberCommandRepository;
	private final MemberQueryRepository memberQueryRepository;
	private final NicknameGenerator nicknameGenerator;
	private final PasswordEncoder passwordEncoder;

	public MemberRegisterService(
		MemberCommandRepository memberCommandRepository,
		MemberQueryRepository memberQueryRepository,
		NicknameGenerator nicknameGenerator, PasswordEncoder passwordEncoder
	) {
		this.memberCommandRepository = memberCommandRepository;
		this.memberQueryRepository = memberQueryRepository;
		this.nicknameGenerator = nicknameGenerator;
		this.passwordEncoder = passwordEncoder;
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

		// TODO: 인증 시스템 구현 후 이메일 인증 토큰 발송 로직 추가 필요
		return new MemberRegistration.Response(saved.getId());
	}

	@Override
	public void activateSignUpMember(SignUpConfirmation.Request request) {
		// TODO: 인증 시스템 구현 후 구현 필요
		throw new UnsupportedOperationException("인증 시스템 구현 필요");
	}
}
