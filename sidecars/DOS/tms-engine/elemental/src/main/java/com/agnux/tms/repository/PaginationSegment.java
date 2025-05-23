package com.agnux.tms.repository;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaginationSegment<T> {

    private final List<T> data;
    private final int totalElements;
    private final int totalPages;
}
