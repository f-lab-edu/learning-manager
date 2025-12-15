package me.chan99k.learningmanager.admin;

import java.sql.Timestamp;

import org.springframework.stereotype.Repository;

import com.querydsl.sql.SQLQueryFactory;

@Repository
public class SystemRoleAuditionAdapter implements SystemRoleAuditionPort {

	private final SQLQueryFactory queryFactory;

	public SystemRoleAuditionAdapter(SQLQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	@Override
	public void save(SystemRoleChangeEvent event) {
		String action = switch (event) {
			case SystemRoleChangeEvent.Granted g -> "GRANTED";
			case SystemRoleChangeEvent.Revoked r -> "REVOKED";
		};

		QMemberSystemRoleAuditLog log = QMemberSystemRoleAuditLog.auditLog;

		queryFactory.insert(log)
			.columns(log.memberId, log.systemRole, log.action,
				log.performedBy, log.performedAt, log.reason)
			.values(event.memberId(), event.role().name(), action,
				event.performedBy(), Timestamp.from(event.performedAt()),
				event.reason())
			.execute();
	}
}
