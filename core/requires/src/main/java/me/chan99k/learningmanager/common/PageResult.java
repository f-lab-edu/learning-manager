package me.chan99k.learningmanager.common;

import java.util.List;
import java.util.function.Function;

/**
 * 페이지네이션 결과를 나타내는 불변 객체.
 * Spring Data의 Page를 대체합니다.
 *
 * @param content 현재 페이지의 내용
 * @param page 현재 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @param totalElements 전체 요소 수
 * @param totalPages 전체 페이지 수
 * @param hasNext 다음 페이지 존재 여부
 * @param <T> 요소 타입
 */
public record PageResult<T>(
	List<T> content,
	int page,
	int size,
	long totalElements,
	int totalPages,
	boolean hasNext
) {
	/**
	 * PageRequest와 전체 요소 수로부터 PageResult 생성.
	 */
	public static <T> PageResult<T> of(List<T> content, PageRequest pageRequest, long totalElements) {
		int totalPages = pageRequest.size() == 0 ? 0 : (int)Math.ceil((double)totalElements / pageRequest.size());
		boolean hasNext = pageRequest.page() + 1 < totalPages;
		return new PageResult<>(
			content,
			pageRequest.page(),
			pageRequest.size(),
			totalElements,
			totalPages,
			hasNext
		);
	}

	/**
	 * 개별 값으로부터 PageResult 생성.
	 */
	public static <T> PageResult<T> of(List<T> content, int page, int size, long totalElements) {
		int totalPages = size == 0 ? 0 : (int)Math.ceil((double)totalElements / size);
		boolean hasNext = page + 1 < totalPages;
		return new PageResult<>(content, page, size, totalElements, totalPages, hasNext);
	}

	/**
	 * 빈 결과 생성.
	 */
	public static <T> PageResult<T> empty(PageRequest pageRequest) {
		return new PageResult<>(List.of(), pageRequest.page(), pageRequest.size(), 0, 0, false);
	}

	/**
	 * 내용을 다른 타입으로 변환.
	 */
	public <R> PageResult<R> map(Function<T, R> mapper) {
		List<R> mappedContent = content.stream().map(mapper).toList();
		return new PageResult<>(mappedContent, page, size, totalElements, totalPages, hasNext);
	}

	/**
	 * 첫 페이지 여부.
	 */
	public boolean isFirst() {
		return page == 0;
	}

	/**
	 * 마지막 페이지 여부.
	 */
	public boolean isLast() {
		return !hasNext;
	}

	/**
	 * 빈 결과 여부.
	 */
	public boolean isEmpty() {
		return content.isEmpty();
	}

	/**
	 * 현재 페이지의 요소 수.
	 */
	public int getNumberOfElements() {
		return content.size();
	}
}