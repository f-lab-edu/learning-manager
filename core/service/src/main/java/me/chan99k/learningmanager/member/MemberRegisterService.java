package me.chan99k.learningmanager.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MemberRegisterService implements MemberRegistration, AccountAddition, SignUpConfirmation {
	private final MemberCommandRepository memberCommandRepository;

	private final MemberQueryRepository memberQueryRepository;

	private final NicknameGenerator nicknameGenerator;

	public MemberRegisterService(
		MemberCommandRepository memberCommandRepository,
		MemberQueryRepository memberQueryRepository,
		NicknameGenerator nicknameGenerator
	) {
		this.memberCommandRepository = memberCommandRepository;
		this.memberQueryRepository = memberQueryRepository;
		this.nicknameGenerator = nicknameGenerator;
	}

	@Override
	public MemberRegistration.Response register(MemberRegistration.Request request) {
		Member newMember = Member.registerDefault(nicknameGenerator);
		newMember.addAccount(request.email());

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
