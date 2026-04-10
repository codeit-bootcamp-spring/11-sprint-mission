package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.PageResponse;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class PageMapper {

    // Slice → PageResponse (totalElements 없음)
    public <T, R> PageResponse<R> toResponse(Slice<T> slice, Function<T, R> mapper) {
        List<R> content = slice.getContent().stream()
                .map(mapper)
                .toList();
        return new PageResponse<>(
                content,
                slice.getNumber(),
                slice.getSize(),
                null    // Slice는 전체 개수 모름
        );
    }

    // Page → PageResponse (totalElements 있음)
    public <T, R> PageResponse<R> toResponse(Page<T> page, Function<T, R> mapper) {
        List<R> content = page.getContent().stream()
                .map(mapper)
                .toList();
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }
}