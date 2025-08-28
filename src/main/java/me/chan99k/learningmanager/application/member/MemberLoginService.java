package me.chan99k.learningmanager.application.member;

import static me.chan99k.learningmanager.domain.member.MemberProblemCode.*;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import me.chan99k.learningmanager.application.member.provides.MemberLogin;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.member.CredentialProvider;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.PasswordEncoder;

@Service
@Transactional
public class MemberLoginService implements MemberLogin {
	private final MemberQueryRepository memberQueryRepository;
	private final PasswordEncoder passwordEncoder;
	private final CredentialProvider credentialProvider;

	public MemberLoginService(MemberQueryRepository memberQueryRepository, PasswordEncoder passwordEncoder,
		CredentialProvider credentialProvider) {
		this.memberQueryRepository = memberQueryRepository;
		this.passwordEncoder = passwordEncoder;
		this.credentialProvider = credentialProvider;
	}

	@Override
	public MemberLogin.Response login(
		MemberLogin.Request request
	) {
		var email = Email.of(request.email());

		Member member = memberQueryRepository.findByEmail(email)
			.orElseThrow(() -> new DomainException(ACCOUNT_NOT_FOUND));

		if (!member.validateLogin(email, request.password(), passwordEncoder)) {
			throw new DomainException(INVALID_CREDENTIAL);
		}

		String issuedCredential = credentialProvider.issueCredential(member);
		return new MemberLogin.Response(issuedCredential);
	}
}
