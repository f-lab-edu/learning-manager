package me.chan99k.learningmanager.admin;

import java.time.Instant;

import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.RelationalPathBase;

@SuppressWarnings("squid:S2160")
public class QMemberSystemRoleAuditLog extends RelationalPathBase<QMemberSystemRoleAuditLog> {

	public static final QMemberSystemRoleAuditLog auditLog =
		new QMemberSystemRoleAuditLog("member_system_role_audit_log");

	public final NumberPath<Long> id = createNumber("id", Long.class);
	public final NumberPath<Long> memberId = createNumber("member_id", Long.class);
	public final StringPath systemRole = createString("system_role");
	public final StringPath action = createString("action");
	public final NumberPath<Long> performedBy = createNumber("performed_by", Long.class);
	public final DateTimePath<Instant> performedAt = createDateTime("performed_at", Instant.class);
	public final StringPath reason = createString("reason");

	public QMemberSystemRoleAuditLog(String tableName) {
		super(QMemberSystemRoleAuditLog.class,
			PathMetadataFactory.forVariable(tableName),
			null,
			tableName);
	}
}
