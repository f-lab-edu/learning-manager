package me.chan99k.learningmanager.member.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import me.chan99k.learningmanager.common.MutableEntity;
import me.chan99k.learningmanager.member.AccountStatus;
import me.chan99k.learningmanager.member.Email;
import me.chan99k.learningmanager.member.mapper.EmailConverter;

@Entity
@Table(name = "account")
public class AccountEntity extends MutableEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private MemberEntity member;

	@Enumerated(EnumType.STRING)
	private AccountStatus status;

	@Convert(converter = EmailConverter.class)
	@Column(name = "email", nullable = false, unique = true)
	private Email email;

	@ElementCollection
	@CollectionTable(
		name = "credential",
		joinColumns = @JoinColumn(name = "account_id")
	)
	private List<CredentialEmbeddable> credentials = new ArrayList<>();

	public AccountEntity() {
	}

	public MemberEntity getMember() {
		return member;
	}

	public void setMember(MemberEntity member) {
		this.member = member;
	}

	public AccountStatus getStatus() {
		return status;
	}

	public void setStatus(AccountStatus status) {
		this.status = status;
	}

	public Email getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = Email.of(email);
	}

	public List<CredentialEmbeddable> getCredentials() {
		return credentials;
	}

	public void setCredentials(List<CredentialEmbeddable> credentials) {
		this.credentials = credentials;
	}
}
