package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.PageResponse;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class PageResponseMapper {

    public <T> PageResponse<T> fromSlice(Slice<T> slice, Function<T, Object> cursorExtractor) {
        List<T> content = slice.getContent();
        Object nextCursor = slice.hasNext() && !content.isEmpty()
                ? cursorExtractor.apply(content.get(content.size() - 1))
                : null;
        return PageResponse.<T>builder()
                .content(content)
                .nextCursor(nextCursor)
                .size(slice.getSize())
                .hasNext(slice.hasNext())
                .totalElements(null)
                .build();
    }

    public <T> PageResponse<T> fromPage(Page<T> page, Function<T, Object> cursorExtractor) {
        List<T> content = page.getContent();
        Object nextCursor = page.hasNext() && !content.isEmpty()
                ? cursorExtractor.apply(content.get(content.size() - 1))
                : null;
        return PageResponse.<T>builder()
                .content(content)
                .nextCursor(nextCursor)
                .size(page.getSize())
                .hasNext(page.hasNext())
                .totalElements(page.getTotalElements())
                .build();
    }
}
