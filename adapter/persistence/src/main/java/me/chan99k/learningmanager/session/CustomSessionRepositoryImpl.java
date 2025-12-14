package me.chan99k.learningmanager.session;

import static me.chan99k.learningmanager.course.entity.QCourseEntity.*;
import static me.chan99k.learningmanager.course.entity.QCourseMemberEntity.*;
import static me.chan99k.learningmanager.course.entity.QCurriculumEntity.*;
import static me.chan99k.learningmanager.session.entity.QSessionEntity.*;
import static me.chan99k.learningmanager.session.entity.QSessionParticipantEntity.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import me.chan99k.learningmanager.course.CourseRole;
import me.chan99k.learningmanager.session.dto.SessionInfo;
import me.chan99k.learningmanager.session.entity.SessionEntity;

@Repository
public class CustomSessionRepositoryImpl implements CustomSessionRepository {

	private final JPAQueryFactory queryFactory;

	public CustomSessionRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	@Override
	public Optional<SessionEntity> findManagedSessionById(Long sessionId, Long memberId, CourseRole courseRole) {
		SessionEntity result = queryFactory
			.selectFrom(sessionEntity)
			.leftJoin(courseEntity).on(sessionEntity.courseId.eq(courseEntity.id))
			.leftJoin(courseEntity.courseMemberList, courseMemberEntity)
			.on(courseMemberEntity.courseRole.eq(courseRole))
			.where(
				sessionEntity.id.eq(sessionId),
				sessionEntity.courseId.isNull()
					.or(courseMemberEntity.memberId.eq(memberId)))
			.fetchOne();

		return Optional.ofNullable(result);
	}

	// ========== 동적 필터 + 페이징 (BooleanBuilder) ==========

	@Override
	public Page<SessionEntity> findAllWithFilters(SessionType type, SessionLocation location,
		Instant startDate, Instant endDate, Pageable pageable) {

		BooleanBuilder builder = createCommonFilterBuilder(type, location, startDate, endDate);

		List<SessionEntity> content = queryFactory
			.selectFrom(sessionEntity)
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(sessionEntity.scheduledAt.desc())
			.fetch();

		JPAQuery<Long> countQuery = queryFactory
			.select(sessionEntity.count())
			.from(sessionEntity)
			.where(builder);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	@Override
	public Page<SessionEntity> findByCourseIdWithFilters(Long courseId, SessionType type, SessionLocation location,
		Instant startDate, Instant endDate, Boolean includeChildSessions, Pageable pageable) {

		BooleanBuilder builder = createCommonFilterBuilder(type, location, startDate, endDate);
		builder.and(sessionEntity.courseId.eq(courseId));

		return findWithFiltersAndPaging(includeChildSessions, pageable, builder);
	}

	@Override
	public Page<SessionEntity> findByCurriculumIdWithFilters(Long curriculumId, SessionType type,
		SessionLocation location,
		Instant startDate, Instant endDate, Boolean includeChildSessions, Pageable pageable) {

		BooleanBuilder builder = createCommonFilterBuilder(type, location, startDate, endDate);
		builder.and(sessionEntity.curriculumId.eq(curriculumId));

		return findWithFiltersAndPaging(includeChildSessions, pageable, builder);
	}

	@Override
	public Page<SessionEntity> findByMemberIdWithFilters(Long memberId, SessionType type, SessionLocation location,
		Instant startDate, Instant endDate, Pageable pageable) {

		BooleanBuilder builder = createCommonFilterBuilder(type, location, startDate, endDate);

		List<SessionEntity> content = queryFactory
			.selectFrom(sessionEntity)
			.join(sessionEntity.participants, sessionParticipantEntity)
			.where(
				builder,
				sessionParticipantEntity.memberId.eq(memberId))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(sessionEntity.scheduledAt.desc())
			.fetch();

		JPAQuery<Long> countQuery = queryFactory
			.select(sessionEntity.count())
			.from(sessionEntity)
			.join(sessionEntity.participants, sessionParticipantEntity)
			.where(
				builder,
				sessionParticipantEntity.memberId.eq(memberId));

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	// ========== 동적 필터 (페이징 없음) ==========

	@Override
	public List<SessionEntity> findByYearMonth(Instant startOfMonth, Instant startOfNextMonth,
		SessionType type, SessionLocation location, Long courseId, Long curriculumId) {

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(sessionEntity.scheduledAt.goe(startOfMonth));
		builder.and(sessionEntity.scheduledAt.lt(startOfNextMonth));

		if (type != null) {
			builder.and(sessionEntity.type.eq(type));
		}
		if (location != null) {
			builder.and(sessionEntity.location.eq(location));
		}
		if (courseId != null) {
			builder.and(sessionEntity.courseId.eq(courseId));
		}
		if (curriculumId != null) {
			builder.and(sessionEntity.curriculumId.eq(curriculumId));
		}

		return queryFactory
			.selectFrom(sessionEntity)
			.where(builder)
			.orderBy(sessionEntity.scheduledAt.asc())
			.fetch();
	}

	@Override
	public List<Long> findIdsByPeriodAndFilters(Instant startDate, Instant endDate, Long courseId, Long curriculumId) {
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(sessionEntity.scheduledAt.gt(startDate));
		builder.and(sessionEntity.scheduledAt.lt(endDate));

		if (courseId != null) {
			builder.and(sessionEntity.courseId.eq(courseId));
		}
		if (curriculumId != null) {
			builder.and(sessionEntity.curriculumId.eq(curriculumId));
		}

		return queryFactory
			.select(sessionEntity.id)
			.from(sessionEntity)
			.where(builder)
			.fetch();
	}

	// ========== DTO Projection ==========

	@Override
	public List<SessionInfo> findSessionInfoProjectionByIds(List<Long> sessionIds) {
		return queryFactory
			.select(Projections.constructor(SessionInfo.class,
				sessionEntity.id,
				sessionEntity.title,
				sessionEntity.scheduledAt,
				sessionEntity.courseId,
				courseEntity.title,
				sessionEntity.curriculumId,
				curriculumEntity.title))
			.from(sessionEntity)
			.leftJoin(courseEntity).on(sessionEntity.courseId.eq(courseEntity.id))
			.leftJoin(curriculumEntity).on(sessionEntity.curriculumId.eq(curriculumEntity.id))
			.where(sessionEntity.id.in(sessionIds))
			.fetch();
	}

	// ========== 프라이빗 메서드  ==========

	private BooleanBuilder createCommonFilterBuilder(SessionType type, SessionLocation location,
		Instant startDate, Instant endDate) {
		BooleanBuilder builder = new BooleanBuilder();

		if (type != null) {
			builder.and(sessionEntity.type.eq(type));
		}
		if (location != null) {
			builder.and(sessionEntity.location.eq(location));
		}
		if (startDate != null) {
			builder.and(sessionEntity.scheduledAt.goe(startDate));
		}
		if (endDate != null) {
			builder.and(sessionEntity.scheduledAt.loe(endDate));
		}

		return builder;
	}

	private Page<SessionEntity> findWithFiltersAndPaging(Boolean includeChildSessions, Pageable pageable,
		BooleanBuilder builder) {
		if (!Boolean.TRUE.equals(includeChildSessions)) {
			builder.and(sessionEntity.parent.isNull());
		}

		List<SessionEntity> content = queryFactory
			.selectFrom(sessionEntity)
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(sessionEntity.scheduledAt.desc())
			.fetch();

		JPAQuery<Long> countQuery = queryFactory
			.select(sessionEntity.count())
			.from(sessionEntity)
			.where(builder);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}
}
