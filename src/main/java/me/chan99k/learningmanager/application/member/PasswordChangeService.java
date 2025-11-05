package me.chan99k.learningmanager.application.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.application.UserContext;
import me.chan99k.learningmanager.application.member.provides.AccountPasswordChange;
import me.chan99k.learningmanager.application.member.requires.MemberCommandRepository;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.domain.exception.DomainException;
import me.chan99k.learningmanager.domain.member.Account;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;
import me.chan99k.learningmanager.domain.member.PasswordEncoder;

@Service
@Transactional
public class PasswordChangeService implements AccountPasswordChange {

	private final MemberQueryRepository memberQueryRepository;
	private final MemberCommandRepository memberCommandRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserContext userContext;

	public PasswordChangeService(MemberQueryRepository memberQueryRepository,
		MemberCommandRepository memberCommandRepository, PasswordEncoder passwordEncoder,
		UserContext userContext) {
		this.memberQueryRepository = memberQueryRepository;
		this.memberCommandRepository = memberCommandRepository;
		this.passwordEncoder = passwordEncoder;
		this.userContext = userContext;
	}

	@Override
	public Response changePassword(Request request) {
		Long memberId = userContext.getCurrentMemberId();

		Member member = memberQueryRepository.findById(memberId)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		Email targetEmail = Email.of(request.email());
		Account targetAccount = member.findAccountByEmail(targetEmail);

		member.changeAccountPassword(targetAccount.getId(), request.newPassword(), passwordEncoder);

		memberCommandRepository.save(member);

		return new Response();
	}
}
