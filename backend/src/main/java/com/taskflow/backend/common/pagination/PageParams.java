package com.taskflow.backend.common.pagination;

public record PageParams(
        int page,
        int limit
) {
    public static PageParams of(Integer page, Integer limit) {
        int resolvedPage = page == null ? 1 : page;
        int resolvedLimit = limit == null ? 20 : limit;

        if (resolvedPage < 1) {
            throw new IllegalArgumentException("page must be greater than or equal to 1");
        }
        if (resolvedLimit < 1) {
            throw new IllegalArgumentException("limit must be greater than or equal to 1");
        }

        return new PageParams(resolvedPage, Math.min(resolvedLimit, 100));
    }
}
