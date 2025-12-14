package me.chan99k.learningmanager.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import me.chan99k.learningmanager.common.BaseEntity;
import me.chan99k.learningmanager.member.SystemRole;

@Entity
@Table(
	name = "member_system_role",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_member_system_role",
		columnNames = {"member_id", "system_role"}
	),
	indexes = @Index(name = "ix_member_system_role_member", columnList = "member_id")
)
public class MemberSystemRoleEntity extends BaseEntity {

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Enumerated(EnumType.STRING)
	@Column(name = "system_role", nullable = false, length = 20)
	private SystemRole systemRole;

	protected MemberSystemRoleEntity() {
	}

	public MemberSystemRoleEntity(Long memberId, SystemRole systemRole) {
		this.memberId = memberId;
		this.systemRole = systemRole;
	}

	public Long getMemberId() {
		return memberId;
	}

	public SystemRole getSystemRole() {
		return systemRole;
	}

}