package me.chan99k.learningmanager.application.member;

import static me.chan99k.learningmanager.domain.member.MemberProblemCode.*;

import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.application.member.provides.AccountAddition;
import me.chan99k.learningmanager.application.member.provides.MemberRegistration;
import me.chan99k.learningmanager.application.member.provides.SignUpConfirmation;
import me.chan99k.learningmanager.domain.exception.DomainException;
import me.chan99k.learningmanager.domain.member.Account;
import me.chan99k.learningmanager.domain.member.EmailSender;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.NicknameGenerator;
import me.chan99k.learningmanager.domain.member.PasswordEncoder;
import me.chan99k.learningmanager.domain.member.SignUpConfirmer;

@Service
@Transactional
public class MemberRegisterService implements MemberRegistration, AccountAddition, SignUpConfirmation {
	private final MemberCommandRepository memberCommandRepository;

	private final MemberQueryRepository memberQueryRepository;

	private final PasswordEncoder passwordEncoder;

	private final NicknameGenerator nicknameGenerator;

	private final SignUpConfirmer signUpConfirmer;

	private final EmailSender emailSender;

	public MemberRegisterService(
		MemberCommandRepository memberCommandRepository,
		MemberQueryRepository memberQueryRepository,
		PasswordEncoder passwordEncoder,
		NicknameGenerator nicknameGenerator,
		SignUpConfirmer signUpConfirmer,
		EmailSender emailSender
	) {
		this.memberCommandRepository = memberCommandRepository;
		this.memberQueryRepository = memberQueryRepository;
		this.passwordEncoder = passwordEncoder;
		this.nicknameGenerator = nicknameGenerator;
		this.signUpConfirmer = signUpConfirmer;
		this.emailSender = emailSender;
	}

	@Override
	public MemberRegistration.Response register(MemberRegistration.Request request) {
		Member newMember = Member.registerDefault(nicknameGenerator);
		newMember.addAccount(request.email(), request.rawPassword(), passwordEncoder);

		Member saved = memberCommandRepository.save(newMember);

		String confirmToken = signUpConfirmer.generateAndStoreToken(
			saved.getId(),
			request.email(),
			Duration.ofHours(24)
		);

		emailSender.sendSignUpConfirmEmail(request.email(), confirmToken);

		return new MemberRegistration.Response(saved.getId());
	}

	@Override
	public void activateSignUpMember(SignUpConfirmation.Request request) {
		if (!signUpConfirmer.validateToken(request.token())) {
			throw new DomainException(EXPIRED_ACTIVATION_TOKEN);
		}

		Long tokenMemberId = signUpConfirmer.getMemberIdByToken(request.token());

		if (tokenMemberId == null) {
			throw new DomainException(INVALID_ACTIVATION_TOKEN);
		}

		Member member = memberQueryRepository.findById(tokenMemberId)
			.orElseThrow(() -> new DomainException(MEMBER_NOT_FOUND));

		List<Account> accounts = member.getAccounts();
		if (accounts.isEmpty()) {
			throw new DomainException(ACCOUNT_NOT_FOUND);
		}

		member.activate();
		member.activateAccount(accounts.get(0).getId());

		memberCommandRepository.save(member);
		signUpConfirmer.removeToken(request.token());
	}
}
