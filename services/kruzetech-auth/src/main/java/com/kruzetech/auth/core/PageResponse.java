package com.kruzetech.auth.core;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/** {items, meta} giống PaginatedResponse của Nest. */
public record PageResponse<T>(List<T> items, Meta meta) {

    public record Meta(int itemCount, long totalItems, int itemsPerPage, int totalPages, int currentPage) {}

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        List<T> items = page.getContent().stream().map(mapper).toList();
        Meta meta = new Meta(
                items.size(),
                page.getTotalElements(),
                page.getSize(),
                Math.max(page.getTotalPages(), 1),
                page.getNumber() + 1);
        return new PageResponse<>(items, meta);
    }
}
