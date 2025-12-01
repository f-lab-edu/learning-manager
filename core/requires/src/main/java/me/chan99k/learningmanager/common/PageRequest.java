package me.chan99k.learningmanager.common;

/**
 * 페이지네이션 요청을 나타내는 불변 객체.
 * Spring Data의 Pageable을 대체합니다.
 *
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @param sortBy 정렬 기준 필드 (nullable)
 * @param sortOrder 정렬 방향 (nullable)
 */
public record PageRequest(
	int page,
	int size,
	String sortBy,
	SortOrder sortOrder
) {
	public PageRequest {
		if (page < 0) {
			throw new IllegalArgumentException("Page must be >= 0");
		}
		if (size <= 0) {
			throw new IllegalArgumentException("Size must be > 0");
		}
	}

	/**
	 * 정렬 없이 페이지 요청 생성.
	 */
	public static PageRequest of(int page, int size) {
		return new PageRequest(page, size, null, null);
	}

	/**
	 * 정렬 포함 페이지 요청 생성.
	 */
	public static PageRequest of(int page, int size, String sortBy, SortOrder sortOrder) {
		return new PageRequest(page, size, sortBy, sortOrder);
	}

	/**
	 * 오프셋 계산.
	 */
	public int getOffset() {
		return page * size;
	}

	/**
	 * 정렬 여부 확인.
	 */
	public boolean hasSort() {
		return sortBy != null && !sortBy.isBlank();
	}
}