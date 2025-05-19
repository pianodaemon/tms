package com.agnux.tms.api.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

class PaginationHelper {

    private static final int DEFAULT_PER_PAGE = 10;
    private static final int DEFAULT_PAGE = 1;
    private static final String DEFAULT_ORDER_BY = "id";
    private static final String DEFAULT_ORDER = "asc";

    @Getter
    @AllArgsConstructor
    public static class PageInfo {
        private final int perPage;
        private final int page;
        private final String orderBy;
        private final String order;

        public int getOffset() {
            return perPage * (page - 1);
        }

        public int getLimit(int totalItems) {
            return Math.min(perPage, Math.max(0, totalItems - getOffset()));
        }

        public int getTotalPages(int totalItems) {
            return (int) Math.ceil((double) totalItems / perPage);
        }
    }

    public static PageInfo extractPageInfo(Map<String, String> pageParams) {
        final int perPage = parseOrDefault(pageParams.get("per_page"), DEFAULT_PER_PAGE);
        final int page = parseOrDefault(pageParams.get("page"), DEFAULT_PAGE);
        final String orderBy = pageParams.getOrDefault("order_by", DEFAULT_ORDER_BY);
        final String order = pageParams.getOrDefault("order", DEFAULT_ORDER);

        return new PageInfo(perPage, page, orderBy, order);
    }

    private static Integer parseOrDefault(String value, int defaultVal) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
