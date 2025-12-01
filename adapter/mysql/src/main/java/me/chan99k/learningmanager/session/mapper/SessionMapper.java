package me.chan99k.learningmanager.session.mapper;

import java.util.ArrayList;
import java.util.List;

import me.chan99k.learningmanager.domain.session.Session;
import me.chan99k.learningmanager.domain.session.SessionParticipant;
import me.chan99k.learningmanager.session.entity.SessionEntity;
import me.chan99k.learningmanager.session.entity.SessionParticipantEntity;

public final class SessionMapper {

	private SessionMapper() {
	}

	public static SessionEntity toEntity(Session domain) {
		if (domain == null) {
			return null;
		}

		SessionEntity entity = new SessionEntity();
		entity.setId(domain.getId());
		entity.setCourseId(domain.getCourseId());
		entity.setCurriculumId(domain.getCurriculumId());
		entity.setTitle(domain.getTitle());
		entity.setScheduledAt(domain.getScheduledAt());
		entity.setScheduledEndAt(domain.getScheduledEndAt());
		entity.setType(domain.getType());
		entity.setLocation(domain.getLocation());
		entity.setLocationDetails(domain.getLocationDetails());
		entity.setCreatedAt(domain.getCreatedAt());
		entity.setCreatedBy(domain.getCreatedBy());
		entity.setLastModifiedAt(domain.getLastModifiedAt());
		entity.setLastModifiedBy(domain.getLastModifiedBy());
		entity.setVersion(domain.getVersion());

		List<SessionParticipantEntity> participantEntities = domain.getParticipants().stream()
			.map(p -> toParticipantEntity(p, entity))
			.toList();
		entity.setParticipants(new ArrayList<>(participantEntities));

		List<SessionEntity> childEntities = domain.getChildren().stream()
			.map(child -> {
				SessionEntity childEntity = toEntity(child);
				childEntity.setParent(entity);
				return childEntity;
			})
			.toList();
		entity.setChildren(new ArrayList<>(childEntities));

		return entity;
	}

	public static Session toDomain(SessionEntity entity) {
		if (entity == null) {
			return null;
		}

		return toDomainWithParent(entity, null);
	}

	private static Session toDomainWithParent(SessionEntity entity, Session parentDomain) {
		List<SessionParticipant> participants = entity.getParticipants().stream()
			.map(SessionMapper::toParticipantDomain)
			.toList();

		Session session = Session.reconstitute(
			entity.getId(),
			entity.getCourseId(),
			entity.getCurriculumId(),
			parentDomain,
			entity.getTitle(),
			entity.getScheduledAt(),
			entity.getScheduledEndAt(),
			entity.getType(),
			entity.getLocation(),
			entity.getLocationDetails(),
			participants,
			entity.getCreatedAt(),
			entity.getCreatedBy(),
			entity.getLastModifiedAt(),
			entity.getLastModifiedBy(),
			entity.getVersion()
		);

		List<Session> children = entity.getChildren().stream()
			.map(childEntity -> toDomainWithParent(childEntity, session))
			.toList();
		session.setChildren(children);

		return session;
	}

	private static SessionParticipantEntity toParticipantEntity(SessionParticipant domain,
		SessionEntity sessionEntity) {
		SessionParticipantEntity entity = new SessionParticipantEntity();
		entity.setId(domain.getId());
		entity.setSession(sessionEntity);
		entity.setMemberId(domain.getMemberId());
		entity.setRole(domain.getRole());
		entity.setCreatedAt(domain.getCreatedAt());
		entity.setCreatedBy(domain.getCreatedBy());
		entity.setLastModifiedAt(domain.getLastModifiedAt());
		entity.setLastModifiedBy(domain.getLastModifiedBy());
		entity.setVersion(domain.getVersion());
		return entity;
	}

	private static SessionParticipant toParticipantDomain(SessionParticipantEntity entity) {
		return SessionParticipant.reconstitute(
			entity.getId(),
			entity.getMemberId(),
			entity.getRole(),
			entity.getCreatedAt(),
			entity.getCreatedBy(),
			entity.getLastModifiedAt(),
			entity.getLastModifiedBy(),
			entity.getVersion()
		);
	}
}
