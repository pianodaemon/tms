package com.agnux.tms.repository;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Segment<T> {

    private final List<T> data;
    private final int totalItems;
    private final int totalPages;
}
