package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.PageResponse;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class PageResponseMapper<T> {

  public PageResponse<T> fromSlice(Slice<T> slice, Instant nextCursor) {
    return new PageResponse<>(
        slice.getContent(),
        nextCursor,
        slice.getSize(),
        slice.hasNext(),
        null
    );
  }

  public PageResponse<T> fromPage(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        null,
        page.getSize(),
        page.hasNext(),
        page.getTotalElements()
    );
  }
}
