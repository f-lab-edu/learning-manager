package me.chan99k.learningmanager.course;

import static me.chan99k.learningmanager.course.entity.QCourseEntity.*;
import static me.chan99k.learningmanager.course.entity.QCourseMemberEntity.*;
import static me.chan99k.learningmanager.course.entity.QCurriculumEntity.*;
import static me.chan99k.learningmanager.member.entity.QAccountEntity.*;
import static me.chan99k.learningmanager.member.entity.QMemberEntity.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import me.chan99k.learningmanager.course.entity.CourseEntity;
import me.chan99k.learningmanager.member.CourseParticipationInfo;

@Repository
public class CustomCourseRepositoryImpl implements CustomCourseRepository {
	private final JPAQueryFactory queryFactory;

	public CustomCourseRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	@Override
	public Page<CourseMemberInfo> findCourseMembersByCourseId(Long courseId, Pageable pageable) {
		List<Tuple> tuples = queryFactory
			.select(
				courseMemberEntity.memberId,
				memberEntity.nickname,
				accountEntity.email,
				courseMemberEntity.courseRole,
				courseMemberEntity.createdAt
			).from(courseMemberEntity)
			.join(memberEntity).on(courseMemberEntity.memberId.eq(memberEntity.id))
			.join(accountEntity).on(accountEntity.member.id.eq(memberEntity.id))
			.where(courseMemberEntity.course.id.eq(courseId))
			.orderBy(courseMemberEntity.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		List<CourseMemberInfo> courseMemberInfos = tuples.stream().map(tuple ->
			new CourseMemberInfo(
				tuple.get(courseMemberEntity.memberId),
				tuple.get(memberEntity.nickname),
				Objects.requireNonNull(tuple.get(accountEntity.email)).address(),
				tuple.get(courseMemberEntity.courseRole),
				tuple.get(courseMemberEntity.createdAt))
		).toList();

		JPAQuery<Long> countQuery = queryFactory
			.select(courseMemberEntity.count())
			.from(courseMemberEntity)
			.where(courseMemberEntity.course.id.eq(courseId));

		return PageableExecutionUtils.getPage(courseMemberInfos, pageable, countQuery::fetchOne);
	}

	@Override
	public Optional<CourseEntity> findManagedCourseById(Long courseId, Long memberId) {
		CourseEntity result = queryFactory
			.selectFrom(courseEntity)
			.join(courseEntity.courseMemberList, courseMemberEntity)
			.where(
				courseEntity.id.eq(courseId),
				courseMemberEntity.memberId.eq(memberId),
				courseMemberEntity.courseRole.eq(CourseRole.MANAGER))
			.fetchOne();

		return Optional.ofNullable(result);
	}

	@Override
	public List<CourseEntity> findManagedCoursesByMemberId(Long memberId) {
		return queryFactory
			.selectFrom(courseEntity)
			.join(courseEntity.courseMemberList, courseMemberEntity)
			.where(
				courseMemberEntity.memberId.eq(memberId),
				courseMemberEntity.courseRole.eq(CourseRole.MANAGER))
			.fetch();
	}

	@Override
	public List<CourseEntity> findParticipatingCoursesByMemberId(Long memberId) {
		return queryFactory
			.selectFrom(courseEntity)
			.join(courseEntity.courseMemberList, courseMemberEntity)
			.where(courseMemberEntity.memberId.eq(memberId))
			.fetch();
	}

	@Override
	public List<CourseParticipationInfo> findParticipatingCoursesWithRoleByMemberId(Long memberId) {
		return queryFactory
			.select(Projections.constructor(CourseParticipationInfo.class,
				courseEntity.id,
				courseEntity.title,
				courseEntity.description,
				courseMemberEntity.courseRole))
			.from(courseEntity)
			.join(courseEntity.courseMemberList, courseMemberEntity)
			.where(courseMemberEntity.memberId.eq(memberId))
			.fetch();
	}

	@Override
	public Optional<CourseDetailInfo> findCourseBasicDetailsById(Long courseId) {
		CourseDetailInfo result = queryFactory
			.select(Projections.constructor(CourseDetailInfo.class,
				courseEntity.id,
				courseEntity.title,
				courseEntity.description,
				courseEntity.createdAt,
				courseMemberEntity.id.countDistinct(),
				curriculumEntity.id.countDistinct()))
			.from(courseEntity)
			.leftJoin(courseEntity.courseMemberList, courseMemberEntity)
			.leftJoin(courseEntity.curriculumList, curriculumEntity)
			.where(courseEntity.id.eq(courseId))
			.groupBy(
				courseEntity.id,
				courseEntity.title,
				courseEntity.description,
				courseEntity.createdAt)
			.fetchOne();

		return Optional.ofNullable(result);
	}

	@Override
	public boolean existsByMemberIdAndCourseIdAndRole(Long memberId, Long courseId, CourseRole role) {
		Integer result = queryFactory
			.selectOne()
			.from(courseMemberEntity)
			.where(
				courseMemberEntity.memberId.eq(memberId),
				courseMemberEntity.course.id.eq(courseId),
				courseMemberEntity.courseRole.eq(role))
			.fetchFirst();

		return result != null;
	}

	@Override
	public boolean existsByMemberIdAndCourseIdAndRoleIn(Long memberId, Long courseId, List<CourseRole> roles) {
		Integer result = queryFactory
			.selectOne()
			.from(courseMemberEntity)
			.where(
				courseMemberEntity.memberId.eq(memberId),
				courseMemberEntity.course.id.eq(courseId),
				courseMemberEntity.courseRole.in(roles))
			.fetchFirst();

		return result != null;
	}

	@Override
	public boolean existsByMemberIdAndCourseId(Long memberId, Long courseId) {
		Integer result = queryFactory
			.selectOne()
			.from(courseMemberEntity)
			.where(
				courseMemberEntity.memberId.eq(memberId),
				courseMemberEntity.course.id.eq(courseId))
			.fetchFirst();

		return result != null;
	}
}
