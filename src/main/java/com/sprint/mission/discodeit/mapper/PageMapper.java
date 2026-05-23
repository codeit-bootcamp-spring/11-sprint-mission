package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.common.PageResponse;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class PageMapper {

  public <T> PageResponse<T> fromSlice(Slice<T> slice, Function<T, Object> cursorExtractor) {
    return new PageResponse<T>(
        slice.getContent(),
        extractNextCursor(slice, cursorExtractor),
        slice.getSize(),
        slice.hasNext(),
        null
    );
  }

  public <T> PageResponse<T> fromPage(Page<T> page, Function<T, Object> cursorExtractor) {
    return new PageResponse<T>(
        page.getContent(),
        extractNextCursor(page, cursorExtractor),
        page.getSize(),
        page.hasNext(),
        page.getTotalElements()
    );
  }

  private <T> Object extractNextCursor(Slice<T> slice, Function<T, Object> cursorExtractor) {
    if (!slice.hasNext() || !slice.hasContent()) {
      return null;
    }
    List<T> content = slice.getContent();
    return cursorExtractor.apply(content.get(content.size() - 1));
  }
}