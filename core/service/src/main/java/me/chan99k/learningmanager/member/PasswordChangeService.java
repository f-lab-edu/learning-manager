package me.chan99k.learningmanager.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.authentication.PasswordEncoder;
import me.chan99k.learningmanager.exception.DomainException;

@Service
@Transactional
public class PasswordChangeService implements PasswordChange {

	private final MemberQueryRepository memberQueryRepository;
	private final MemberCommandRepository memberCommandRepository;
	private final PasswordEncoder passwordEncoder;

	public PasswordChangeService(
		MemberQueryRepository memberQueryRepository,
		MemberCommandRepository memberCommandRepository,
		PasswordEncoder passwordEncoder
	) {
		this.memberQueryRepository = memberQueryRepository;
		this.memberCommandRepository = memberCommandRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void changePassword(Long memberId, Request request) {
		Member member = memberQueryRepository.findById(memberId)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		Email primaryEmail = member.getPrimaryEmail();
		Account account = member.findAccountByEmail(primaryEmail);
		Credential credential = account.findCredentialByType(CredentialType.PASSWORD);

		if (!passwordEncoder.matches(request.currentPassword(), credential.getSecret())) {
			throw new DomainException(MemberProblemCode.INVALID_CREDENTIAL);
		}

		if (passwordEncoder.matches(request.newPassword(), credential.getSecret())) {
			throw new DomainException(MemberProblemCode.NEW_PASSWORD_SAME_AS_CURRENT);
		}

		String encoded = passwordEncoder.encode(request.newPassword());
		account.changePasswordCredential(encoded);

		memberCommandRepository.save(member);
	}
}
