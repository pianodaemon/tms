package com.agnux.tms.repository;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaginationSegment<D> {

    private final List<D> data;
    private final int totalElements;
    private final int totalPages;
}
