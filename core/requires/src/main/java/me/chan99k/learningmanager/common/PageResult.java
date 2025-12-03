package me.chan99k.learningmanager.common;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(
	List<T> content,
	int page,
	int size,
	long totalElements,
	int totalPages,
	boolean hasNext
) {

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

	public <R> PageResult<R> map(Function<T, R> mapper) {
		List<R> mappedContent = content.stream().map(mapper).toList();
		return new PageResult<>(mappedContent, page, size, totalElements, totalPages, hasNext);
	}

	public boolean isFirst() {
		return page == 0;
	}

	public boolean isLast() {
		return !hasNext;
	}

	public boolean isEmpty() {
		return content.isEmpty();
	}

	public int getNumberOfElements() {
		return content.size();
	}
}