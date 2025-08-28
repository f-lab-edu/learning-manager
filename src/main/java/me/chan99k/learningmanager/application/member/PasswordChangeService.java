package me.chan99k.learningmanager.application.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.application.member.provides.AccountPasswordChange;
import me.chan99k.learningmanager.application.member.requires.MemberCommandRepository;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.common.exception.DomainException;
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

	public PasswordChangeService(MemberQueryRepository memberQueryRepository,
		MemberCommandRepository memberCommandRepository, PasswordEncoder passwordEncoder) {
		this.memberQueryRepository = memberQueryRepository;
		this.memberCommandRepository = memberCommandRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public Response changePassword(Request request) {
		Long memberId = AuthenticationContextHolder.getCurrentMemberId()
			.orElseThrow(() -> new IllegalStateException("[System] 인증된 사용자의 컨텍스트를 찾을 수 없습니다"));

		Member member = memberQueryRepository.findById(memberId)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		Email targetEmail = Email.of(request.email());
		Account targetAccount = member.findAccountByEmail(targetEmail);

		member.changeAccountPassword(targetAccount.getId(), request.newPassword(), passwordEncoder);

		memberCommandRepository.save(member);

		return new Response();
	}
}
