package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class PageResponseMapper {
    public <T, R> PageResponse<R> toDto(Slice<T> slice, Function<T, R> contentMapper) {
        return new PageResponse<>(
                slice.getContent().stream()
                        .map(contentMapper)
                        .toList(),
                slice.getNumber(),
                slice.getSize(),
                slice.hasNext(),
                null
        );
    }

    public <T, R> PageResponse<R> toDto(Page<T> page, Function<T, R> contentMapper) {
        return new PageResponse<>(
                page.getContent().stream()
                        .map(contentMapper)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.hasNext(),
                page.getTotalElements()
        );
    }
}
