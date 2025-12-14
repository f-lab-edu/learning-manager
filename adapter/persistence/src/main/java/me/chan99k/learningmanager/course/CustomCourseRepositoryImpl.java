package me.chan99k.learningmanager.course;

import static me.chan99k.learningmanager.course.entity.QCourseMemberEntity.*;
import static me.chan99k.learningmanager.member.entity.QAccountEntity.*;
import static me.chan99k.learningmanager.member.entity.QMemberEntity.*;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

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
}
