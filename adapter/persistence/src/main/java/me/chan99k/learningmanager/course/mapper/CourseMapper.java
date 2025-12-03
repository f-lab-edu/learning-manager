package me.chan99k.learningmanager.course.mapper;

import java.util.ArrayList;
import java.util.List;

import me.chan99k.learningmanager.course.Course;
import me.chan99k.learningmanager.course.CourseMember;
import me.chan99k.learningmanager.course.Curriculum;
import me.chan99k.learningmanager.course.entity.CourseEntity;
import me.chan99k.learningmanager.course.entity.CourseMemberEntity;
import me.chan99k.learningmanager.course.entity.CurriculumEntity;

public final class CourseMapper {

	private CourseMapper() {
	}

	public static CourseEntity toEntity(Course domain) {
		if (domain == null) {
			return null;
		}

		CourseEntity entity = new CourseEntity();
		entity.setId(domain.getId());
		entity.setTitle(domain.getTitle());
		entity.setDescription(domain.getDescription());
		entity.setCreatedAt(domain.getCreatedAt());
		entity.setCreatedBy(domain.getCreatedBy());
		entity.setLastModifiedAt(domain.getLastModifiedAt());
		entity.setLastModifiedBy(domain.getLastModifiedBy());
		entity.setVersion(domain.getVersion());

		List<CourseMemberEntity> memberEntities = domain.getCourseMemberList().stream()
			.map(cm -> toCourseMemberEntity(cm, entity))
			.toList();
		entity.setCourseMemberList(new ArrayList<>(memberEntities));

		List<CurriculumEntity> curriculumEntities = domain.getCurriculumList().stream()
			.map(c -> toCurriculumEntity(c, entity))
			.toList();
		entity.setCurriculumList(new ArrayList<>(curriculumEntities));

		return entity;
	}

	public static Course toDomain(CourseEntity entity) {
		if (entity == null) {
			return null;
		}

		List<CourseMember> courseMembers = entity.getCourseMemberList().stream()
			.map(CourseMapper::toCourseMemberDomain)
			.toList();

		List<Curriculum> curriculums = entity.getCurriculumList().stream()
			.map(CourseMapper::toCurriculumDomain)
			.toList();

		return Course.reconstitute(
			entity.getId(),
			entity.getTitle(),
			entity.getDescription(),
			courseMembers,
			curriculums,
			entity.getCreatedAt(),
			entity.getCreatedBy(),
			entity.getLastModifiedAt(),
			entity.getLastModifiedBy(),
			entity.getVersion()
		);
	}

	private static CourseMemberEntity toCourseMemberEntity(CourseMember domain, CourseEntity courseEntity) {
		CourseMemberEntity entity = new CourseMemberEntity();
		entity.setId(domain.getId());
		entity.setCourse(courseEntity);
		entity.setMemberId(domain.getMemberId());
		entity.setCourseRole(domain.getCourseRole());
		entity.setCreatedAt(domain.getCreatedAt());
		entity.setCreatedBy(domain.getCreatedBy());
		entity.setLastModifiedAt(domain.getLastModifiedAt());
		entity.setLastModifiedBy(domain.getLastModifiedBy());
		entity.setVersion(domain.getVersion());
		return entity;
	}

	private static CourseMember toCourseMemberDomain(CourseMemberEntity entity) {
		return CourseMember.reconstitute(
			entity.getId(),
			entity.getMemberId(),
			entity.getCourseRole(),
			entity.getCreatedAt(),
			entity.getCreatedBy(),
			entity.getLastModifiedAt(),
			entity.getLastModifiedBy(),
			entity.getVersion()
		);
	}

	private static CurriculumEntity toCurriculumEntity(Curriculum domain, CourseEntity courseEntity) {
		CurriculumEntity entity = new CurriculumEntity();
		entity.setId(domain.getId());
		entity.setCourse(courseEntity);
		entity.setTitle(domain.getTitle());
		entity.setDescription(domain.getDescription());
		entity.setCreatedAt(domain.getCreatedAt());
		entity.setCreatedBy(domain.getCreatedBy());
		entity.setLastModifiedAt(domain.getLastModifiedAt());
		entity.setLastModifiedBy(domain.getLastModifiedBy());
		entity.setVersion(domain.getVersion());
		return entity;
	}

	private static Curriculum toCurriculumDomain(CurriculumEntity entity) {
		return Curriculum.reconstitute(
			entity.getId(),
			entity.getTitle(),
			entity.getDescription(),
			entity.getCreatedAt(),
			entity.getCreatedBy(),
			entity.getLastModifiedAt(),
			entity.getLastModifiedBy(),
			entity.getVersion()
		);
	}
}
