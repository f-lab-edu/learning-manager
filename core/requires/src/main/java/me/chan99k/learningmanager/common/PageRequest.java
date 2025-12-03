package me.chan99k.learningmanager.common;

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

	public static PageRequest of(int page, int size) {
		return new PageRequest(page, size, null, null);
	}

	public static PageRequest of(int page, int size, String sortBy, SortOrder sortOrder) {
		return new PageRequest(page, size, sortBy, sortOrder);
	}

	public int getOffset() {
		return page * size;
	}

	public boolean hasSort() {
		return sortBy != null && !sortBy.isBlank();
	}
}