package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class PageResponseMapper {
    public <T, R> PageResponse<R> toDto(Slice<T> slice, Function<T, R> contentMapper) {
        return new PageResponse<>(
                slice.getContent().stream()
                        .map(contentMapper)
                        .toList(),
                null,
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
                null,
                page.getSize(),
                page.hasNext(),
                page.getTotalElements()
        );
    }

    public <T, R> PageResponse<R> toCursorDto(List<T> items, int pageSize,
                                              Function<T, R> contentMapper, Function<T, Object> cursorMapper) {
        boolean hasNext = items.size() > pageSize;
        List<T> pageItems = hasNext
                ? items.subList(0, pageSize)
                : items;

        Object nextCursor = null;
        if(hasNext && !pageItems.isEmpty()) {
            nextCursor = cursorMapper.apply(pageItems.get(pageItems.size()-1));
        }

        return new PageResponse<>(
                pageItems.stream()
                        .map(contentMapper)
                        .toList(),
                nextCursor,
                pageItems.size(),
                hasNext,
                null
        );
    }
}
